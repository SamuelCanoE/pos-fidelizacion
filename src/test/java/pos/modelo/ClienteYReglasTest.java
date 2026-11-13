package pos.modelo;
import org.junit.jupiter.api.Test;
import pos.modelo.Modelo.Cliente;
import static org.junit.jupiter.api.Assertions.*;

class ClienteYReglasTest {
    @Test void laPrioridadDependeDelEmbarazoYLaEdad() {
        assertEquals(0, new Cliente(1, "A", 29, true, 0).prioridadEnFila());    // embarazo
        assertEquals(1, new Cliente(2, "B", 65, false, 0).prioridadEnFila());   // tercera edad
        assertEquals(2, new Cliente(3, "C", 64, false, 0).prioridadEnFila());   // fila normal
    }
    @Test void elEmbarazoTienePrioridadSobreLaEdad() { assertEquals(0, new Cliente(4, "D", 70, true, 0).prioridadEnFila()); }
    @Test void descuentoSoloConElUmbralDePuntos() {
        assertEquals(0, Reglas.descuento(25_000, 19));
        assertEquals(2_500, Reglas.descuento(25_000, 20));
        assertEquals(2_500, Reglas.descuento(25_000, 500));
    }
    @Test void unPuntoPorCadaMilPesos() {
        assertEquals(0, Reglas.puntosGanados(999)); assertEquals(1, Reglas.puntosGanados(1_000)); assertEquals(22, Reglas.puntosGanados(22_500));
    }
}
