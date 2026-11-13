package pos.adt;
/** ADT de grupos familiares que comparten puntos. El id del cliente es el índice (base 1). */
public interface IFamilias {
    void unir(int padre, int hijo);
    boolean mismaFamilia(int a, int b);
    int raiz(int id);
    void sumarPuntos(int id, long puntos);
    long puntosGrupo(int id);
    int[] padres(int hasta);   // arreglo "padre" de los ids 0..hasta (el 0 no se usa)
}
