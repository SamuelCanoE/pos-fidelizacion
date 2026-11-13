package pos.ds;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ColaPrioridadTest {
    @Test void extraeSiempreElMenor() {
        ColaPrioridad<Integer> h = new ColaPrioridad<>(Comparator.<Integer>naturalOrder());
        for (int x : new int[]{5, 3, 8, 1, 9, 2}) h.ingresar(x);
        assertEquals(1, h.proximo());
        StringBuilder sb = new StringBuilder(); while (h.size() > 0) sb.append(h.atender()).append(' ');
        assertEquals("1 2 3 5 8 9 ", sb.toString());
    }
    @Test void coincideConOrdenarUnArregloAleatorio() {
        Random r = new Random(42); int[] datos = r.ints(2000, 0, 500).toArray();
        ColaPrioridad<Integer> h = new ColaPrioridad<>(Comparator.<Integer>naturalOrder());
        for (int d : datos) h.ingresar(d);
        int[] esperado = datos.clone(); Arrays.sort(esperado);
        for (int e : esperado) assertEquals(e, h.atender());
        assertNull(h.atender());
    }
    @Test void empataPorOrdenDeLlegadaConUnComparadorCompuesto() {
        record T(int prioridad, int seq) {}
        ColaPrioridad<T> h = new ColaPrioridad<>(Comparator.comparingInt(T::prioridad).thenComparingInt(T::seq));
        h.ingresar(new T(1, 1)); h.ingresar(new T(0, 2)); h.ingresar(new T(1, 3)); h.ingresar(new T(0, 4));
        assertEquals(new T(0, 2), h.atender()); assertEquals(new T(0, 4), h.atender());
        assertEquals(new T(1, 1), h.atender()); assertEquals(new T(1, 3), h.atender());
    }
    @Test void verNoAlteraLaCola() {
        ColaPrioridad<Integer> h = new ColaPrioridad<>(Comparator.<Integer>naturalOrder());
        h.ingresar(3); h.ingresar(1); h.ingresar(2);
        assertEquals(List.of(1, 2, 3), h.ver()); assertEquals(3, h.size()); assertEquals(1, h.proximo());
    }
    @Test void internoEsUnHeapValido() {
        ColaPrioridad<Integer> h = new ColaPrioridad<>(Comparator.<Integer>naturalOrder());
        for (int x : new int[]{9, 4, 7, 1, 8, 2, 6}) h.ingresar(x);
        List<Integer> a = h.interno();   // posición i (base 1) = índice i-1; el padre de i es i/2
        for (int i = 2; i <= a.size(); i++) assertTrue(a.get(i / 2 - 1) <= a.get(i - 1));
    }
}
