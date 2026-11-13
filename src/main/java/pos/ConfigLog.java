package pos;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;

/** Logs del sistema: consola + archivo rotativo logs/pos-0.log + las últimas líneas en memoria para mostrarlas en la web. */
public final class ConfigLog {
    private ConfigLog() {}
    private static final Bitacora BITACORA = new Bitacora();

    static final class Bitacora extends Handler {
        private final Deque<String> lineas = new ArrayDeque<>();
        public synchronized void publish(LogRecord r) {
            if (!isLoggable(r)) return;
            lineas.addLast(getFormatter().format(r).stripTrailing());
            while (lineas.size() > 100) lineas.removeFirst();
        }
        synchronized List<String> ultimas(int n) { List<String> l = new ArrayList<>(lineas); return l.subList(Math.max(0, l.size() - n), l.size()); }
        public void flush() {}
        public void close() {}
    }
    public static List<String> ultimas(int n) { return BITACORA.ultimas(n); }

    public static void iniciar(Level nivel) {
        Formatter formato = new Formatter() {
            public String format(LogRecord r) {
                String origen = r.getLoggerName() == null ? "root" : r.getLoggerName().substring(r.getLoggerName().lastIndexOf('.') + 1);
                String s = String.format("%1$tF %1$tT [%2$s] %3$s - %4$s%n", r.getMillis(), r.getLevel().getName(), origen, formatMessage(r));
                if (r.getThrown() != null) { StringWriter sw = new StringWriter(); r.getThrown().printStackTrace(new PrintWriter(sw)); s += sw; }
                return s;
            }
        };
        Logger raiz = Logger.getLogger("");
        for (Handler h : raiz.getHandlers()) raiz.removeHandler(h);
        raiz.setLevel(nivel);
        Handler consola = new ConsoleHandler(); consola.setFormatter(formato); consola.setLevel(Level.ALL); raiz.addHandler(consola);
        BITACORA.setFormatter(formato); BITACORA.setLevel(Level.ALL); raiz.addHandler(BITACORA);
        try {
            Files.createDirectories(Path.of("logs"));
            Handler archivo = new FileHandler("logs/pos-%g.log", 1_000_000, 3, true);
            archivo.setEncoding("UTF-8"); archivo.setFormatter(formato); archivo.setLevel(Level.ALL); raiz.addHandler(archivo);
        } catch (IOException e) { raiz.warning("No se pudo abrir logs/pos-0.log: " + e.getMessage()); }
    }
}
