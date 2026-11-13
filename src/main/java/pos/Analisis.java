package pos;
import java.util.*;
import pos.ds.*;

/** Mediciones de rendimiento: cada estructura propia frente a la alternativa "ingenua". Ejecutar: clic derecho > Run 'Analisis.main()'. */
public class Analisis {
    static long ms(Runnable r) { long t = System.nanoTime(); r.run(); return (System.nanoTime() - t) / 1_000_000; }

    public static void main(String[] args) {
        calentar();
        System.out.println("\n1) ¿Misma familia? 1.000 consultas: Union-Find vs. recorrer la tabla (SELECT sin índice, O(N))");
        System.out.printf("%-12s %-16s %-16s%n", "clientes", "UnionFind (ms)", "Tabla O(N) (ms)");
        for (int n : new int[]{1_000, 10_000, 100_000, 1_000_000}) {
            int[] fam = new int[n + 1]; UnionFind uf = new UnionFind();
            for (int i = 1; i <= n; i++) { fam[i] = (i - 1) / 5; if (i % 5 != 1) uf.unir(i - 1, i); }
            Random r = new Random(1); int[][] qs = new int[1000][2];
            for (int[] p : qs) { p[0] = 1 + r.nextInt(n); p[1] = 1 + r.nextInt(n); }
            int[] c = new int[2];
            long a = ms(() -> { for (int[] p : qs) if (uf.mismaFamilia(p[0], p[1])) c[0]++; });
            long b = ms(() -> { for (int[] p : qs) { int fa = -1, fb = -1; for (int i = 1; i <= n; i++) { if (i == p[0]) fa = fam[i]; if (i == p[1]) fb = fam[i]; } if (fa == fb) c[1]++; } });
            if (c[0] != c[1]) throw new IllegalStateException("Resultados distintos");
            System.out.printf("%-12d %-16d %-16d%n", n, a, b);
        }
        System.out.println("\n2) Caja preferencial: ingresar n y atender n. Min-Heap O(n log n) vs. lista + buscar el mínimo O(n²)");
        System.out.printf("%-12s %-16s %-16s%n", "clientes", "MinHeap (ms)", "Lista (ms)");
        for (int n : new int[]{1_000, 10_000, 50_000}) {
            int[] datos = new Random(2).ints(n, 0, 3).toArray();
            long a = ms(() -> { ColaPrioridad<Integer> h = new ColaPrioridad<>(Comparator.<Integer>naturalOrder()); for (int d : datos) h.ingresar(d); while (h.size() > 0) h.atender(); });
            long b = ms(() -> {
                List<Integer> l = new ArrayList<>(); for (int d : datos) l.add(d);
                while (!l.isEmpty()) { int mi = 0; for (int i = 1; i < l.size(); i++) if (l.get(i) < l.get(mi)) mi = i; l.remove(mi); }
            });
            System.out.printf("%-12d %-16d %-16d%n", n, a, b);
        }
        System.out.println("\n3) Fila normal: ingresar n y atender n. Cola enlazada O(1) vs. ArrayList.remove(0) O(n)");
        System.out.printf("%-12s %-16s %-16s%n", "clientes", "Cola (ms)", "ArrayList (ms)");
        for (int n : new int[]{10_000, 50_000, 100_000}) {
            long a = ms(() -> { Cola<Integer> q = new Cola<>(); for (int i = 0; i < n; i++) q.ingresar(i); while (q.size() > 0) q.atender(); });
            long b = ms(() -> { List<Integer> q = new ArrayList<>(); for (int i = 0; i < n; i++) q.add(i); while (!q.isEmpty()) q.remove(0); });
            System.out.printf("%-12d %-16d %-16d%n", n, a, b);
        }
    }
    static void calentar() {   // calienta el JIT para que la primera fila no salga sesgada
        UnionFind u = new UnionFind(); for (int i = 2; i < 5000; i++) u.unir(i - 1, i);
        ColaPrioridad<Integer> h = new ColaPrioridad<>(Comparator.<Integer>naturalOrder()); for (int i = 0; i < 5000; i++) h.ingresar(i % 7); while (h.size() > 0) h.atender();
        Cola<Integer> c = new Cola<>(); for (int i = 0; i < 5000; i++) c.ingresar(i); while (c.size() > 0) c.atender();
    }
}
