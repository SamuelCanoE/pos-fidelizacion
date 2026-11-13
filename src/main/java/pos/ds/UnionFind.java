package pos.ds;
import java.util.Arrays;
import pos.adt.IFamilias;
/**
 * Conjuntos disjuntos con arreglos de base 1: el índice ES el id del cliente (la posición 0 no se usa).
 * Unión por tamaño + compresión de camino (path halving) => O(α(n)) amortizado por operación.
 * Los puntos del grupo se acumulan solo en la raíz, así que consultarlos cuesta lo mismo que un find.
 */
public class UnionFind implements IFamilias {
    private int[] padre = new int[0], tam = new int[0]; private long[] pts = new long[0];
    private void asegurar(int id) {
        if (id < padre.length) return;
        int viejo = padre.length, m = Math.max(id + 1, Math.max(16, viejo * 2));
        padre = Arrays.copyOf(padre, m); tam = Arrays.copyOf(tam, m); pts = Arrays.copyOf(pts, m);
        for (int i = viejo; i < m; i++) { padre[i] = i; tam[i] = 1; }
    }
    public int raiz(int x) {
        asegurar(x);
        while (padre[x] != x) { padre[x] = padre[padre[x]]; x = padre[x]; }
        return x;
    }
    public void unir(int p, int h) {
        int a = raiz(p), b = raiz(h);
        if (a == b) return;
        if (tam[a] < tam[b]) { int t = a; a = b; b = t; }
        padre[b] = a; tam[a] += tam[b]; pts[a] += pts[b];
    }
    public boolean mismaFamilia(int a, int b) { return raiz(a) == raiz(b); }
    public void sumarPuntos(int id, long p) { int r = raiz(id); pts[r] += p; }
    public long puntosGrupo(int id) { int r = raiz(id); return pts[r]; }
    public int[] padres(int hasta) { asegurar(hasta); return Arrays.copyOf(padre, hasta + 1); }
}
