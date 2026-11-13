package pos.ds;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ListaDobleTest {
    @Test void recorreEnAmbosSentidos() {
        ListaDoble<Integer> l = new ListaDoble<>(); l.agregarFinal(1); l.agregarFinal(2); l.agregarFinal(3);
        assertEquals(List.of(1, 2, 3), l.desdeElInicio()); assertEquals(List.of(3, 2, 1), l.desdeElFinal()); assertEquals(3, l.size());
    }
    @Test void listaVaciaNoFalla() {
        ListaDoble<Integer> l = new ListaDoble<>();
        assertTrue(l.desdeElInicio().isEmpty()); assertTrue(l.desdeElFinal().isEmpty());
    }
    @Test void unSoloElementoEsCabezaYCola() {
        ListaDoble<String> l = new ListaDoble<>(); l.agregarFinal("x");
        assertEquals(List.of("x"), l.desdeElInicio()); assertEquals(List.of("x"), l.desdeElFinal());
    }
}
