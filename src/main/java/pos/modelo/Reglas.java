package pos.modelo;
/** Reglas de negocio en un solo lugar (fáciles de ajustar y de probar). */
public final class Reglas {
    private Reglas() {}
    public static final int EDAD_ADULTO_MAYOR = 65;     // tercera edad: caja preferencial desde esta edad
    public static final int UMBRAL_PUNTOS = 20;         // puntos del grupo familiar para activar el descuento
    public static final int DESCUENTO_PORCENTAJE = 10;
    public static final int PESOS_POR_PUNTO = 1000;     // 1 punto por cada $1.000 pagados
    public static long descuento(long subtotal, long puntosGrupo) { return puntosGrupo >= UMBRAL_PUNTOS ? subtotal * DESCUENTO_PORCENTAJE / 100 : 0; }
    public static int puntosGanados(long total) { return (int) (total / PESOS_POR_PUNTO); }
}
