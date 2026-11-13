package pos.ds;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ColaTest {
    @Test void esFifo() {
        Cola<String> c = new Cola<>(); c.ingresar("ana"); c.ingresar("beto"); c.ingresar("carla");
        assertEquals("ana", c.atender()); assertEquals("beto", c.proximo()); assertEquals(2, c.size());
        assertEquals(List.of("beto", "carla"), c.ver());
    }
    @Test void colaVaciaDevuelveNull() { assertNull(new Cola<Integer>().atender()); assertNull(new Cola<Integer>().proximo()); }
    @Test void sigueFuncionandoDespuesDeVaciarse() {
        Cola<Integer> c = new Cola<>(); c.ingresar(1); c.atender(); c.ingresar(2); c.ingresar(3);
        assertEquals(2, c.atender()); assertEquals(3, c.atender()); assertEquals(0, c.size());
    }
}
