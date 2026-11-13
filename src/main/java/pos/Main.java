package pos;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.concurrent.Executors;
import java.util.logging.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import pos.repo.RepositorioJdbc;
import pos.servicio.*;
import pos.web.Api;

public class Main {
    static String env(String k, String d) { String v = System.getenv(k); return v == null || v.isBlank() ? d : v; }

    public static void main(String[] args) throws Exception {
        Level nivel; try { nivel = Level.parse(env("POS_LOG_NIVEL", "INFO")); } catch (IllegalArgumentException e) { nivel = Level.INFO; }
        ConfigLog.iniciar(nivel);
        Logger log = Logger.getLogger(Main.class.getName());
        String url = env("POS_DB_URL", "jdbc:postgresql://localhost:5432/pos");
        Connection cx;
        try { cx = DriverManager.getConnection(url, env("POS_DB_USER", "postgres"), env("POS_DB_PASS", "postgres")); }
        catch (SQLException e) {
            log.severe("No se pudo conectar a PostgreSQL (" + url + "): " + e.getMessage());
            log.severe("Revise: 1) que PostgreSQL esté activo, 2) que exista la base 'pos', 3) la contraseña (variable POS_DB_PASS).");
            return;
        }
        RepositorioJdbc repo = new RepositorioJdbc(cx);
        try (var in = Main.class.getResourceAsStream("/schema.sql")) { repo.crearEsquema(new String(in.readAllBytes(), UTF_8)); }
        Tienda tienda = new Tienda(repo, new Metricas());

        int puerto = Integer.parseInt(env("POS_PUERTO", "8080"));
        HttpServer srv = HttpServer.create(new InetSocketAddress(puerto), 0);
        srv.createContext("/", ex -> { try (var in = Main.class.getResourceAsStream("/web/index.html")) { Api.responder(ex, 200, "text/html; charset=utf-8", in.readAllBytes()); } });
        srv.createContext("/api", new Api(tienda));
        srv.setExecutor(Executors.newFixedThreadPool(4));
        srv.start();
        log.info("POS listo en http://localhost:" + puerto);
        System.out.println("POS listo en http://localhost:" + puerto);
    }
}
