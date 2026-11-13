package pos.servicio;
import java.util.*;
import java.util.logging.Logger;
import pos.adt.*;
import pos.ds.*;
import pos.modelo.Modelo.*;
import pos.modelo.Reglas;
import pos.repo.Repositorio;

/** Lógica del negocio: estructuras de datos en memoria para lo rápido, el Repositorio (PostgreSQL) para lo permanente. */
public class Tienda {
    private static final Logger LOG = Logger.getLogger(Tienda.class.getName());
    private final Repositorio repo;
    private final Metricas metricas;
    private final IFamilias familias = new UnionFind();
    private final IFila<Turno> normal = new Cola<>();
    private final IFila<Turno> preferencial = new ColaPrioridad<>(Comparator.comparingInt(Turno::prioridad).thenComparingLong(Turno::seq));
    private final ListaDoble<Recibo> historial = new ListaDoble<>();
    private final Set<Integer> enCaja = new LinkedHashSet<>();   // clientes en una fila o siendo atendidos: evita duplicados en O(1)
    private ICarrito carrito;
    private Turno atendiendo;
    private long seq;

    public Tienda(Repositorio repo, Metricas metricas) {
        this.repo = repo; this.metricas = metricas;
        for (Cliente c : repo.clientes()) familias.sumarPuntos(c.id(), c.puntos());
        int uniones = 0;
        for (int[] u : repo.uniones()) { familias.unir(u[0], u[1]); uniones++; }
        for (Recibo r : repo.ventasDeHoy()) historial.agregarFinal(r);
        LOG.info("Tienda lista: " + uniones + " uniones familiares reconstruidas en Union-Find y " + historial.size() + " ventas de hoy en la lista doble");
    }
    public Metricas metricas() { return metricas; }
    private void exigirVenta() { if (atendiendo == null) throw new IllegalStateException("No hay un cliente en caja. Llame al siguiente de una fila."); }

    public synchronized Map<String, Object> estado() {
        List<Cliente> clientes = metricas.medir("PostgreSQL · listar clientes", repo::clientes);
        List<Producto> productos = metricas.medir("PostgreSQL · listar productos", repo::productos);
        List<Map<String, Object>> cls = new ArrayList<>(), items = new ArrayList<>();
        int maxId = 0;
        for (Cliente c : clientes) {
            maxId = Math.max(maxId, c.id());
            cls.add(Map.of("id", c.id(), "nombre", c.nombre(), "edad", c.edad(), "embarazada", c.embarazada(), "puntos", c.puntos(), "familia", familias.raiz(c.id()), "puntosGrupo", familias.puntosGrupo(c.id())));
        }
        long sub = 0;
        if (carrito != null) {
            for (var en : carrito.resumen().entrySet()) items.add(Map.of("id", en.getKey().id(), "nombre", en.getKey().nombre(), "cantidad", en.getValue(), "subtotal", en.getKey().precio() * en.getValue()));
            sub = carrito.subtotal();
        }
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("reglas", Map.of("umbralPuntos", Reglas.UMBRAL_PUNTOS, "descuentoPorcentaje", Reglas.DESCUENTO_PORCENTAJE, "edadAdultoMayor", Reglas.EDAD_ADULTO_MAYOR, "pesosPorPunto", Reglas.PESOS_POR_PUNTO));
        e.put("productos", productos); e.put("clientes", cls); e.put("enCaja", new ArrayList<>(enCaja));
        e.put("normal", normal.ver()); e.put("preferencial", preferencial.ver());
        e.put("atendiendo", atendiendo); e.put("items", items); e.put("subtotal", sub);
        e.put("descuento", atendiendo == null ? 0 : Reglas.descuento(sub, familias.puntosGrupo(atendiendo.clienteId())));
        e.put("historial", historial.desdeElFinal());
        e.put("estructuras", estructuras(maxId));
        e.put("metricas", metricas.resumen());
        return e;
    }
    /** Fotografía didáctica del interior de cada estructura. */
    private Map<String, Object> estructuras(int maxId) {
        Map<String, Object> m = new LinkedHashMap<>();
        List<Recibo> h = historial.desdeElInicio();
        m.put("cola", normal.interno());
        m.put("heap", preferencial.interno());
        m.put("pila", carrito == null ? List.of() : carrito.acciones().stream().map(Producto::nombre).toList());
        m.put("bolsa", carrito == null ? List.of() : carrito.bolsa().stream().map(Producto::nombre).toList());
        m.put("lista", h.size() > 8 ? h.subList(h.size() - 8, h.size()) : h);
        m.put("listaTotal", h.size());
        m.put("padres", familias.padres(maxId));
        return m;
    }

    public synchronized int crearCliente(String nombre, int edad, boolean embarazada) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre es obligatorio");
        if (edad < 0 || edad > 120) throw new IllegalArgumentException("La edad debe estar entre 0 y 120");
        int id = metricas.medir("PostgreSQL · crear cliente", () -> repo.crearCliente(nombre.trim(), edad, embarazada));
        LOG.info("Cliente nuevo #" + id + " (" + nombre.trim() + ")");
        return id;
    }
    public synchronized void unir(int padre, int hijo) {
        if (padre == hijo) throw new IllegalArgumentException("Elija dos clientes distintos");
        if (repo.cliente(padre).isEmpty() || repo.cliente(hijo).isEmpty()) throw new IllegalArgumentException("El cliente no existe");
        if (familias.mismaFamilia(padre, hijo)) throw new IllegalStateException("Ya pertenecen a la misma familia");
        metricas.ejecutar("PostgreSQL · guardar unión familiar", () -> repo.guardarUnion(padre, hijo));
        metricas.ejecutar("UnionFind.unir · O(α(n))", () -> familias.unir(padre, hijo));
        LOG.info("Familia: cliente " + hijo + " unido bajo el cliente " + padre + " (puntos compartidos: " + familias.puntosGrupo(padre) + ")");
    }
    public synchronized boolean mismaFamilia(int a, int b) {
        return metricas.medir("UnionFind.mismaFamilia · O(α(n))", () -> familias.mismaFamilia(a, b));
    }

    /** Un cliente llega a pagar: va a la fila normal o a la preferencial según su prioridad (embarazo, tercera edad). */
    public synchronized String llegada(int clienteId) {
        Cliente c = metricas.medir("PostgreSQL · buscar cliente", () -> repo.cliente(clienteId)).orElseThrow(() -> new IllegalArgumentException("El cliente no existe"));
        if (!enCaja.add(clienteId)) throw new IllegalStateException(c.nombre() + " ya está en una fila o en caja");
        Turno t = new Turno(c.id(), c.nombre(), c.prioridadEnFila(), ++seq);
        String caja;
        if (t.prioridad() < 2) { metricas.ejecutar("MinHeap.ingresar · O(log n)", () -> preferencial.ingresar(t)); caja = "preferencial"; }
        else { metricas.ejecutar("Cola.ingresar · O(1)", () -> normal.ingresar(t)); caja = "normal"; }
        LOG.info(c.nombre() + " llegó → caja " + caja + " (prioridad " + t.prioridad() + ")");
        return caja;
    }
    public synchronized void siguiente(String caja) {
        if (atendiendo != null) throw new IllegalStateException("Cobre o cancele la venta actual primero");
        boolean pref = "preferencial".equals(caja);
        Turno t = pref ? metricas.medir("MinHeap.atender · O(log n)", preferencial::atender) : metricas.medir("Cola.atender · O(1)", normal::atender);
        if (t == null) throw new IllegalStateException("La fila está vacía");
        atendiendo = t; carrito = new Carrito();
        LOG.info("Se llamó a " + t.nombre() + " de la caja " + (pref ? "preferencial" : "normal"));
    }
    public synchronized void escanear(int productoId) {
        exigirVenta();
        Producto p = metricas.medir("PostgreSQL · buscar producto", () -> repo.producto(productoId)).orElseThrow(() -> new IllegalArgumentException("El producto no existe"));
        metricas.ejecutar("Bag.add + Pila.push · O(1)", () -> carrito.agregar(p));
        LOG.fine("Escaneado " + p.nombre());
    }
    public synchronized void deshacer() {
        exigirVenta();
        Producto p = metricas.medir("Pila.pop (deshacer) · O(1)", carrito::deshacer);
        if (p == null) throw new IllegalStateException("No hay nada que deshacer");
        LOG.info("Deshacer: se quitó " + p.nombre() + " del carrito de " + atendiendo.nombre());
    }
    public synchronized void cancelar() {
        exigirVenta();
        LOG.info("Venta cancelada para " + atendiendo.nombre());
        enCaja.remove(atendiendo.clienteId()); atendiendo = null; carrito = null;
    }
    public synchronized Recibo cobrar() {
        exigirVenta();
        if (carrito.cantidad() == 0) throw new IllegalStateException("El carrito está vacío");
        final int cid = atendiendo.clienteId();
        final String nombre = atendiendo.nombre();
        final long sub = carrito.subtotal(), desc = Reglas.descuento(sub, familias.puntosGrupo(cid)), total = sub - desc;
        final int pts = Reglas.puntosGanados(total);
        final Map<Producto, Integer> items = carrito.resumen();
        Recibo rc = metricas.medir("PostgreSQL · cobrar (transacción)", () -> repo.guardarVenta(cid, nombre, items, sub, desc, total, pts));
        metricas.ejecutar("UnionFind.sumarPuntos · O(α(n))", () -> familias.sumarPuntos(cid, pts));
        metricas.ejecutar("ListaDoble.agregarFinal · O(1)", () -> historial.agregarFinal(rc));
        enCaja.remove(cid); atendiendo = null; carrito = null;
        LOG.info(String.format("Venta #%d cobrada a %s: subtotal=%d descuento=%d total=%d puntos=+%d", rc.id(), nombre, sub, desc, total, pts));
        return rc;
    }
}
