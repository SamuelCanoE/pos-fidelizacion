package pos.adt;
/** ADT del cliente: qué sabe de sí mismo y cómo se ubica en las filas, sin exponer su almacenamiento. */
public interface ICliente {
    int id();
    String nombre();
    int edad();
    boolean embarazada();
    /** 0 = embarazo, 1 = adulto mayor (tercera edad), 2 = fila normal. Menor número = se atiende antes. */
    int prioridadEnFila();
}
