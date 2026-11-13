package pos.modelo;
import pos.adt.ICliente;
public final class Modelo {
    public record Producto(int id, String nombre, long precio) {}
    public record Cliente(int id, String nombre, int edad, boolean embarazada, int puntos) implements ICliente {
        public int prioridadEnFila() {
            if (embarazada) return 0;
            return edad >= Reglas.EDAD_ADULTO_MAYOR ? 1 : 2;
        }
    }
    /** prioridad: 0 = embarazo, 1 = adulto mayor (65+), 2 = normal. */
    public record Turno(int clienteId, String nombre, int prioridad, long seq) {}
    public record Recibo(int id, String cliente, long subtotal, long descuento, long total, int puntos, String hora) {}
}
