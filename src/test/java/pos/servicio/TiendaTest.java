package pos.servicio;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.*;
import pos.modelo.Modelo.*;
import static org.junit.jupiter.api.Assertions.*;

class TiendaTest {
    RepositorioMemoria repo; Tienda t;
    @BeforeEach void iniciar() { repo = new RepositorioMemoria(); t = new Tienda(repo, new Metricas()); }
    Turno atendiendo() { return (Turno) t.estado().get("atendiendo"); }
    long subtotal() { return (Long) t.estado().get("subtotal"); }

    @Test @DisplayName("Los clientes se reparten entre fila normal y preferencial")
    void reparteEntreFilas() {
        assertEquals("normal", t.llegada(1));
        assertEquals("preferencial", t.llegada(2));
        assertEquals("preferencial", t.llegada(3));
    }
    @Test @DisplayName("Min-Heap: el embarazo pasa por delante de la tercera edad aunque llegó después")
    void embarazoGanaALaTerceraEdad() {
        t.llegada(5); t.llegada(3);
        t.siguiente("preferencial");
        assertEquals("Laura Ruiz", atendiendo().nombre());
    }
    @Test @DisplayName("Con igual prioridad se atiende por orden de llegada")
    void empateSeResuelvePorLlegada() {
        t.llegada(5); t.llegada(2);
        t.siguiente("preferencial");
        assertEquals("Marta Soto", atendiendo().nombre());
    }
    @Test @DisplayName("La fila normal es FIFO")
    void filaNormalEsFifo() {
        t.llegada(1); t.llegada(4);
        t.siguiente("normal");
        assertEquals("Ana Gómez", atendiendo().nombre());
    }
    @Test @DisplayName("Flujo completo: escanear, cobrar, puntos e historial")
    void flujoCompleto() {
        t.llegada(4); t.siguiente("normal");
        t.escanear(2); t.escanear(3);
        Recibo r = t.cobrar();
        assertEquals(7_200, r.total()); assertEquals(0, r.descuento()); assertEquals(7, r.puntos());
        assertEquals(7, repo.cliente(4).orElseThrow().puntos());
        assertEquals(1, ((List<?>) t.estado().get("historial")).size());
        assertNull(atendiendo());
    }
    @Test @DisplayName("Pila: 'Borrar último' deshace en orden inverso")
    void deshacerFuncionaEnOrdenInverso() {
        t.llegada(1); t.siguiente("normal");
        t.escanear(2); t.escanear(3);
        t.deshacer(); assertEquals(4_200, subtotal());
        t.deshacer(); assertEquals(0, subtotal());
        assertThrows(IllegalStateException.class, () -> t.deshacer());
    }
    @Test void noSePuedeEscanearDeshacerNiCobrarSinCliente() {
        assertThrows(IllegalStateException.class, () -> t.escanear(1));
        assertThrows(IllegalStateException.class, () -> t.deshacer());
        assertThrows(IllegalStateException.class, () -> t.cobrar());
    }
    @Test void noSeCobraUnCarritoVacio() {
        t.llegada(1); t.siguiente("normal");
        assertThrows(IllegalStateException.class, () -> t.cobrar());
    }
    @Test void productoInexistenteSeRechaza() {
        t.llegada(1); t.siguiente("normal");
        assertThrows(IllegalArgumentException.class, () -> t.escanear(999));
    }
    @Test @DisplayName("Sin familia no hay descuento con 12 puntos")
    void sinFamiliaNoHayDescuento() {
        t.llegada(1); t.siguiente("normal"); t.escanear(4); t.escanear(5);
        assertEquals(0, t.cobrar().descuento());
    }
    @Test @DisplayName("Union-Find: la familia comparte puntos y obtiene el 10 % de descuento")
    void familiaComparteDescuento() {
        t.unir(1, 2);
        t.llegada(1); t.siguiente("normal"); t.escanear(4); t.escanear(5);
        Recibo r = t.cobrar();
        assertEquals(2_500, r.descuento()); assertEquals(22_500, r.total()); assertEquals(22, r.puntos());
        t.llegada(2); t.siguiente("preferencial"); t.escanear(5);   // Carlos tiene 67 años: caja preferencial
        assertEquals(1_100, ((Number) t.estado().get("descuento")).longValue());
    }
    @Test void mismaFamiliaEsTransitiva() {
        t.unir(1, 2); t.unir(2, 3);
        assertTrue(t.mismaFamilia(1, 3)); assertFalse(t.mismaFamilia(1, 4));
    }
    @Test void validaLasUniones() {
        assertThrows(IllegalArgumentException.class, () -> t.unir(1, 1));
        assertThrows(IllegalArgumentException.class, () -> t.unir(1, 99));
        t.unir(1, 2);
        assertThrows(IllegalStateException.class, () -> t.unir(2, 1));
    }
    @Test void unClienteNoPuedeEstarDosVecesEnFila() {
        t.llegada(1);
        assertThrows(IllegalStateException.class, () -> t.llegada(1));
    }
    @Test void validaLaLlegada() {
        assertThrows(IllegalArgumentException.class, () -> t.llegada(99));
    }
    @Test void noSeLlamaAOtroClienteConUnaVentaEnCurso() {
        t.llegada(1); t.llegada(4); t.siguiente("normal");
        assertThrows(IllegalStateException.class, () -> t.siguiente("normal"));
    }
    @Test void cancelarLiberaLaCajaYAlCliente() {
        t.llegada(1); t.siguiente("normal"); t.cancelar();
        assertNull(atendiendo());
        assertEquals("normal", t.llegada(1));
    }
    @Test void llamarUnaFilaVaciaAvisa() { assertThrows(IllegalStateException.class, () -> t.siguiente("preferencial")); }
    @Test @DisplayName("Al reiniciar, familias e historial se reconstruyen desde la base")
    void reconstruyeElEstadoDesdeElRepositorio() {
        t.unir(1, 2); t.llegada(1); t.siguiente("normal"); t.escanear(1); t.cobrar();
        Tienda reiniciada = new Tienda(repo, new Metricas());
        assertTrue(reiniciada.mismaFamilia(1, 2));
        assertEquals(1, ((List<?>) reiniciada.estado().get("historial")).size());
    }
    @Test void crearClienteValidaDatos() {
        assertEquals(6, t.crearCliente("  Nuevo  ", 30, false));
        assertThrows(IllegalArgumentException.class, () -> t.crearCliente(" ", 30, false));
        assertThrows(IllegalArgumentException.class, () -> t.crearCliente("X", 200, false));
    }
    @Test void registraMetricasDeLasEstructuras() {
        t.llegada(3); t.llegada(1);
        var ops = t.metricas().resumen().stream().map(Metricas.Medicion::operacion).toList();
        assertTrue(ops.contains("MinHeap.ingresar · O(log n)")); assertTrue(ops.contains("Cola.ingresar · O(1)"));
    }
    @Test void elEstadoExponeLasEstructurasParaVisualizarlas() {
        t.llegada(3); t.siguiente("preferencial"); t.escanear(1);
        @SuppressWarnings("unchecked") Map<String, Object> e = (Map<String, Object>) t.estado().get("estructuras");
        assertEquals(List.of("Manzana"), e.get("pila")); assertEquals(List.of("Manzana"), e.get("bolsa"));
        assertTrue(e.containsKey("cola") && e.containsKey("heap") && e.containsKey("lista") && e.containsKey("padres"));
    }
}
