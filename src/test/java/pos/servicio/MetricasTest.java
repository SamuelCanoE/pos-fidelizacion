package pos.servicio;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MetricasTest {
    @Test void cuentaLlamadasYDevuelveElResultado() {
        Metricas m = new Metricas();
        int r = m.medir("suma", () -> 2 + 3); m.ejecutar("suma", () -> {});
        assertEquals(5, r);
        var med = m.resumen().get(0);
        assertEquals("suma", med.operacion()); assertEquals(2, med.llamadas()); assertTrue(med.maxUs() >= med.promedioUs());
    }
    @Test void registraAunqueLaOperacionFalle() {
        Metricas m = new Metricas();
        assertThrows(IllegalStateException.class, () -> m.ejecutar("falla", () -> { throw new IllegalStateException(); }));
        assertEquals(1, m.resumen().get(0).llamadas());
    }
    @Test void reiniciarBorraLasMediciones() { Metricas m = new Metricas(); m.ejecutar("x", () -> {}); m.reiniciar(); assertTrue(m.resumen().isEmpty()); }
}
