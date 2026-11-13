package pos.adt;
import java.util.List;
/** ADT de fila de pago: la caja normal (FIFO) y la preferencial (prioridad) cumplen el mismo contrato. */
public interface IFila<T> {
    void ingresar(T x);
    T atender();      // null si está vacía
    T proximo();
    int size();
    List<T> ver();    // en el orden en que serán atendidos
    List<T> interno(); // disposición interna real (útil para visualizar la estructura)
}
