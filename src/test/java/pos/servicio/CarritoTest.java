package pos.servicio;
import org.junit.jupiter.api.Test;
import pos.modelo.Modelo.Producto;
import static org.junit.jupiter.api.Assertions.*;

class CarritoTest {
    private final Producto leche = new Producto(1, "Leche", 4200), pan = new Producto(2, "Pan", 3000);
    @Test void agruparRepetidosEnElResumenYSumarElSubtotal() {
        Carrito c = new Carrito(); c.agregar(leche); c.agregar(pan); c.agregar(leche);
        assertEquals(2, c.resumen().get(leche)); assertEquals(1, c.resumen().get(pan));
        assertEquals(11_400, c.subtotal()); assertEquals(3, c.cantidad());
    }
    @Test void deshacerQuitaSiempreElUltimoEscaneado() {
        Carrito c = new Carrito(); c.agregar(leche); c.agregar(pan);
        assertEquals(pan, c.deshacer()); assertEquals(4_200, c.subtotal());
        assertEquals(leche, c.deshacer()); assertEquals(0, c.cantidad());
    }
    @Test void deshacerConCarritoVacioDevuelveNull() { assertNull(new Carrito().deshacer()); }
    @Test void pilaYBolsaSeMantienenSincronizadas() {
        Carrito c = new Carrito(); c.agregar(leche); c.agregar(pan); c.deshacer();
        assertEquals(c.acciones(), c.bolsa()); assertEquals(1, c.acciones().size());
    }
}
