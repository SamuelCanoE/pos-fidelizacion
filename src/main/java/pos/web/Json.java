package pos.web;
import java.lang.reflect.Array;
import java.util.Map;
/** Serializador JSON mínimo (Map, Iterable, arreglo, record, String, número, booleano) para no depender de librerías. */
public final class Json {
    private Json() {}
    public static String a(Object o) { StringBuilder b = new StringBuilder(); w(b, o); return b.toString(); }
    private static void w(StringBuilder b, Object o) {
        try {
            if (o == null) b.append("null");
            else if (o instanceof Number || o instanceof Boolean) b.append(o);
            else if (o instanceof Map<?, ?> m) {
                b.append('{'); boolean f = true;
                for (var e : m.entrySet()) { if (!f) b.append(','); f = false; s(b, String.valueOf(e.getKey())); b.append(':'); w(b, e.getValue()); }
                b.append('}');
            } else if (o instanceof Iterable<?> it) {
                b.append('['); boolean f = true;
                for (Object x : it) { if (!f) b.append(','); f = false; w(b, x); }
                b.append(']');
            } else if (o.getClass().isArray()) {
                b.append('[');
                for (int i = 0, n = Array.getLength(o); i < n; i++) { if (i > 0) b.append(','); w(b, Array.get(o, i)); }
                b.append(']');
            } else if (o.getClass().isRecord()) {
                b.append('{'); boolean f = true;
                for (var c : o.getClass().getRecordComponents()) { if (!f) b.append(','); f = false; s(b, c.getName()); b.append(':'); w(b, c.getAccessor().invoke(o)); }
                b.append('}');
            } else s(b, o.toString());
        } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
    }
    private static void s(StringBuilder b, String t) {
        b.append('"');
        for (char c : t.toCharArray()) {
            switch (c) {
                case '"' -> b.append("\\\""); case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n"); case '\r' -> b.append("\\r"); case '\t' -> b.append("\\t");
                default -> { if (c < 0x20) b.append(String.format("\\u%04x", (int) c)); else b.append(c); }
            }
        }
        b.append('"');
    }
}
