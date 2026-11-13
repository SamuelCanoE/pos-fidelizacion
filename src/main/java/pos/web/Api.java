package pos.web;
import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;
import java.util.logging.Logger;
import static java.nio.charset.StandardCharsets.UTF_8;
import pos.ConfigLog;
import pos.servicio.Tienda;

/** Capa HTTP: traduce cada ruta /api/... a una operación de la Tienda y responde JSON. */
public class Api implements HttpHandler {
    private static final Logger LOG = Logger.getLogger(Api.class.getName());
    private static final Set<String> LECTURAS = Set.of("/estado", "/familia/mismo");
    private static final Map<String, Object> OK = Map.of("ok", true);
    private final Tienda tienda;
    public Api(Tienda tienda) { this.tienda = tienda; }

    static int n(Map<String, String> q, String k) {
        try { return Integer.parseInt(q.get(k)); } catch (NumberFormatException e) { throw new IllegalArgumentException("Falta o es inválido el dato: " + k); }
    }
    static Map<String, String> params(String raw) {
        Map<String, String> m = new HashMap<>();
        if (raw == null) return m;
        for (String kv : raw.split("&")) { int i = kv.indexOf('='); if (i > 0) m.put(URLDecoder.decode(kv.substring(0, i), UTF_8), URLDecoder.decode(kv.substring(i + 1), UTF_8)); }
        return m;
    }
    public static void responder(HttpExchange ex, int code, String tipo, byte[] b) throws IOException {
        ex.getResponseHeaders().set("Content-Type", tipo); ex.sendResponseHeaders(code, b.length);
        try (var o = ex.getResponseBody()) { o.write(b); }
    }
    public void handle(HttpExchange ex) throws IOException {
        long t0 = System.nanoTime();
        String ruta = ex.getRequestURI().getPath().substring(4), metodo = ex.getRequestMethod();
        Map<String, String> q = params(ex.getRequestURI().getRawQuery());
        int code = 200; String body;
        try {
            if (!LECTURAS.contains(ruta) && !"POST".equals(metodo)) { code = 405; throw new IllegalArgumentException("Use POST para esta operación"); }
            Object r = switch (ruta) {
                case "/estado" -> { Map<String, Object> e = new LinkedHashMap<>(tienda.estado()); e.put("bitacora", ConfigLog.ultimas(40)); yield e; }
                case "/cliente" -> Map.of("id", tienda.crearCliente(q.get("nombre"), n(q, "edad"), "true".equals(q.get("embarazada"))));
                case "/familia" -> { tienda.unir(n(q, "padre"), n(q, "hijo")); yield OK; }
                case "/familia/mismo" -> Map.of("mismo", tienda.mismaFamilia(n(q, "a"), n(q, "b")));
                case "/llegada" -> Map.of("caja", tienda.llegada(n(q, "cliente")));
                case "/siguiente" -> { tienda.siguiente(q.getOrDefault("caja", "normal")); yield OK; }
                case "/escanear" -> { tienda.escanear(n(q, "producto")); yield OK; }
                case "/deshacer" -> { tienda.deshacer(); yield OK; }
                case "/cancelar" -> { tienda.cancelar(); yield OK; }
                case "/cobrar" -> tienda.cobrar();
                case "/metricas/reiniciar" -> { tienda.metricas().reiniciar(); yield OK; }
                default -> { code = 404; throw new IllegalArgumentException("Ruta desconocida: " + ruta); }
            };
            body = Json.a(r);
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (code == 200) code = 400;
            body = Json.a(Map.of("error", String.valueOf(e.getMessage())));
            LOG.warning(metodo + " /api" + ruta + " → " + code + ": " + e.getMessage());
        } catch (Exception e) {
            code = 500; body = Json.a(Map.of("error", "Error interno: " + e.getMessage()));
            LOG.log(java.util.logging.Level.SEVERE, metodo + " /api" + ruta + " falló", e);
        }
        long ms = (System.nanoTime() - t0) / 1_000_000;
        if (code == 200) { if ("/estado".equals(ruta)) LOG.fine(metodo + " /api" + ruta + " (" + ms + " ms)"); else LOG.info(metodo + " /api" + ruta + " → 200 (" + ms + " ms)"); }
        responder(ex, code, "application/json; charset=utf-8", body.getBytes(UTF_8));
    }
}
