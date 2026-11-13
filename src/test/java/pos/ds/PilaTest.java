package pos.ds;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PilaTest {
    @Test void esLifo() {
        Pila<String> p = new Pila<>(); p.push("a"); p.push("b"); p.push("c");
        assertEquals("c", p.pop()); assertEquals("b", p.pop()); assertEquals("a", p.pop());
    }
    @Test void crecePorEncimaDeLaCapacidadInicial() {
        Pila<Integer> p = new Pila<>(); for (int i = 0; i < 100; i++) p.push(i);
        assertEquals(100, p.size());
        for (int i = 99; i >= 0; i--) assertEquals(i, p.pop());
    }
    @Test void popEnPilaVaciaDevuelveNull() { assertNull(new Pila<Integer>().pop()); }
    @Test void contenidoVaDelTopeAlFondoSinModificarLaPila() {
        Pila<Integer> p = new Pila<>(); p.push(1); p.push(2); p.push(3);
        assertEquals(java.util.List.of(3, 2, 1), p.contenido()); assertEquals(3, p.size());
    }
}
