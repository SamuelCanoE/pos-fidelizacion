package pos.ds;
import java.util.Random;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UnionFindTest {
    @Test void inicialmenteCadaClienteEsSuPropiaFamilia() {
        UnionFind u = new UnionFind();
        assertFalse(u.mismaFamilia(1, 2)); assertEquals(7, u.raiz(7));
    }
    @Test void unirEsTransitivo() {
        UnionFind u = new UnionFind(); u.unir(1, 2); u.unir(2, 3);
        assertTrue(u.mismaFamilia(1, 3)); assertFalse(u.mismaFamilia(1, 4));
    }
    @Test void unirDosVecesNoDuplicaPuntos() {
        UnionFind u = new UnionFind(); u.sumarPuntos(1, 12); u.sumarPuntos(2, 9);
        u.unir(1, 2); u.unir(2, 1);
        assertEquals(21, u.puntosGrupo(1)); assertEquals(21, u.puntosGrupo(2));
    }
    @Test void losPuntosSeComparten() {
        UnionFind u = new UnionFind(); u.unir(1, 2); u.sumarPuntos(2, 5); u.sumarPuntos(1, 3);
        assertEquals(8, u.puntosGrupo(1)); assertEquals(8, u.puntosGrupo(2));
        assertEquals(0, u.puntosGrupo(3));
    }
    @Test void elArregloCreceConIdsGrandes() {
        UnionFind u = new UnionFind(); u.unir(5, 100_000);
        assertTrue(u.mismaFamilia(5, 100_000)); assertEquals(100_001, u.padres(100_000).length);
    }
    @Test void laRaizEsPuntoFijo() {
        UnionFind u = new UnionFind(); for (int i = 2; i <= 50; i++) u.unir(i - 1, i);
        int r = u.raiz(50); assertEquals(r, u.raiz(r)); assertEquals(r, u.padres(50)[r]);
        for (int i = 1; i <= 50; i++) assertEquals(r, u.raiz(i));
    }
    @Test void coincideConUnaImplementacionIngenuaEnPruebaAleatoria() {
        int n = 300; Random rnd = new Random(7); UnionFind u = new UnionFind(); int[] grupo = new int[n + 1];
        for (int i = 1; i <= n; i++) grupo[i] = i;
        for (int k = 0; k < 400; k++) {
            int a = 1 + rnd.nextInt(n), b = 1 + rnd.nextInt(n); u.unir(a, b);
            int ga = grupo[a], gb = grupo[b]; for (int i = 1; i <= n; i++) if (grupo[i] == gb) grupo[i] = ga;
        }
        for (int k = 0; k < 2000; k++) { int a = 1 + rnd.nextInt(n), b = 1 + rnd.nextInt(n); assertEquals(grupo[a] == grupo[b], u.mismaFamilia(a, b)); }
    }
}
