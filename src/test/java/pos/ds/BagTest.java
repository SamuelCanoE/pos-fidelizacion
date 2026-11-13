package pos.ds;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BagTest {
    @Test void admiteDuplicadosYCuentaElementos() {
        Bag<String> b = new Bag<>(); b.add("manzana"); b.add("leche"); b.add("manzana");
        assertEquals(3, b.size());
        List<String> vistos = new ArrayList<>(); for (String s : b) vistos.add(s);
        assertEquals(2, Collections.frequency(vistos, "manzana"));
    }
    @Test void quitarUltimoDevuelveElUltimoAgregado() {
        Bag<Integer> b = new Bag<>(); b.add(1); b.add(2); b.add(3);
        assertEquals(3, b.quitarUltimo()); assertEquals(2, b.quitarUltimo()); assertEquals(1, b.size());
    }
    @Test void bolsaVaciaDevuelveNullYNoFalla() {
        Bag<Integer> b = new Bag<>();
        assertNull(b.quitarUltimo()); assertEquals(0, b.size()); assertFalse(b.iterator().hasNext());
    }
}
