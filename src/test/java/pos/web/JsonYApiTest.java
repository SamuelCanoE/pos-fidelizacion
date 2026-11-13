package pos.web;
import java.util.*;
import org.junit.jupiter.api.Test;
import pos.modelo.Modelo.Turno;
import static org.junit.jupiter.api.Assertions.*;

class JsonYApiTest {
    @Test void escapaComillasYSaltosDeLinea() { assertEquals("{\"k\":\"a\\\"b\\n\"}", Json.a(Map.of("k", "a\"b\n"))); }
    @Test void serializaListasArreglosNulosYBooleanos() {
        assertEquals("[1,2]", Json.a(List.of(1, 2))); assertEquals("[0,5,9]", Json.a(new int[]{0, 5, 9}));
        assertEquals("null", Json.a(null)); assertEquals("true", Json.a(true));
    }
    @Test void serializaRecords() {
        assertEquals("{\"clienteId\":1,\"nombre\":\"Ana\",\"prioridad\":0,\"seq\":7}", Json.a(new Turno(1, "Ana", 0, 7)));
    }
    @Test void leeParametrosConCaracteresEspeciales() {
        Map<String, String> q = Api.params("nombre=Mar%C3%ADa%20Paz&edad=30");
        assertEquals("María Paz", q.get("nombre")); assertEquals(30, Api.n(q, "edad"));
    }
    @Test void unNumeroInvalidoGeneraUnErrorClaro() {
        assertThrows(IllegalArgumentException.class, () -> Api.n(Map.of("a", "x"), "a"));
        assertThrows(IllegalArgumentException.class, () -> Api.n(Map.of(), "a"));
    }
}
