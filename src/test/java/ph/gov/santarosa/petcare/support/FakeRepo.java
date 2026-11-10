package ph.gov.santarosa.petcare.support;

import jakarta.persistence.Id;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A tiny in-memory stand-in for a Spring Data repository, so the real service classes can be tested without a
 * database. It understands the same derived-query names the real repositories use (findBy..., existsBy...,
 * countBy..., deleteBy..., ...And..., ...IgnoreCase, ...True/False, ...In, ...OrderBy...Desc, findTopNBy).
 */
public final class FakeRepo {

    private FakeRepo() { }

    @SuppressWarnings("unchecked")
    public static <R> R create(Class<R> repository, Class<?> entity) {
        return (R) Proxy.newProxyInstance(repository.getClassLoader(), new Class<?>[] { repository }, new Handler(entity));
    }

    private static final class Handler implements InvocationHandler {
        private static final Pattern DERIVED = Pattern.compile("^(findAll|findTop(\\d+)|find|exists|count|delete)By(.*)$");
        private final Map<Object, Object> rows = new LinkedHashMap<>();
        private final Field idField;
        private long nextLong = 1;

        Handler(Class<?> entity) {
            Field f = null;
            for (Field x : entity.getDeclaredFields()) if (x.isAnnotationPresent(Id.class)) f = x;
            if (f == null) throw new IllegalStateException("No @Id on " + entity);
            f.setAccessible(true);
            this.idField = f;
        }

        @Override
        public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
            String n = m.getName();
            switch (n) {
                case "save": return save(args[0]);
                case "findById": case "lockByKind": return Optional.ofNullable(rows.get(args[0]));
                case "findAll": return new ArrayList<>(rows.values());
                case "existsById": return rows.containsKey(args[0]);
                case "count": return (long) rows.size();
                case "deleteById": rows.remove(args[0]); return null;
                case "delete": rows.remove(idField.get(args[0])); return null;
                case "toString": return "FakeRepo" + rows.keySet();
                case "hashCode": return System.identityHashCode(proxy);
                case "equals": return proxy == args[0];
                default: return derived(m, args == null ? new Object[0] : args);
            }
        }

        private Object save(Object e) throws Exception {
            if (idField.get(e) == null) {
                if (idField.getType() == Long.class) idField.set(e, nextLong++);
                else throw new IllegalStateException("Entity saved without an id: " + e.getClass().getSimpleName());
            }
            rows.put(idField.get(e), e);
            return e;
        }

        private Object derived(Method m, Object[] args) throws Exception {
            Matcher mm = DERIVED.matcher(m.getName());
            if (!mm.matches()) throw new UnsupportedOperationException("FakeRepo cannot interpret " + m.getName());
            String kind = mm.group(1);
            String[] parts = mm.group(3).split("OrderBy", 2);
            List<Object> found = new ArrayList<>();
            int arg = 0;
            String[] preds = parts[0].isEmpty() ? new String[0] : parts[0].split("And");
            List<Object> matching = new ArrayList<>(rows.values());
            for (String p : preds) {
                String prop; String op = "eq";
                if (p.endsWith("IgnoreCase")) { prop = p.substring(0, p.length() - 10); op = "ic"; }
                else if (p.endsWith("True")) { prop = p.substring(0, p.length() - 4); op = "true"; }
                else if (p.endsWith("False")) { prop = p.substring(0, p.length() - 5); op = "false"; }
                else if (p.endsWith("In")) { prop = p.substring(0, p.length() - 2); op = "in"; }
                else prop = p;
                Object expected = op.equals("true") || op.equals("false") ? null : args[arg++];
                final String fop = op;
                List<Object> next = new ArrayList<>();
                for (Object row : matching) {
                    Object actual = get(row, prop);
                    boolean ok = switch (fop) {
                        case "true" -> Boolean.TRUE.equals(actual);
                        case "false" -> Boolean.FALSE.equals(actual);
                        case "in" -> ((Collection<?>) expected).contains(actual);
                        case "ic" -> actual instanceof String s && expected instanceof String x && s.equalsIgnoreCase(x);
                        default -> actual == null ? expected == null : actual.equals(expected);
                    };
                    if (ok) next.add(row);
                }
                matching = next;
            }
            found.addAll(matching);
            if (parts.length > 1) {
                boolean desc = parts[1].endsWith("Desc");
                String prop = parts[1].replaceAll("(Desc|Asc)$", "");
                @SuppressWarnings({ "unchecked", "rawtypes" })
                Comparator<Object> c = (a, b) -> {
                    try { return ((Comparable) get(a, prop)).compareTo(get(b, prop)); } catch (Exception e) { throw new RuntimeException(e); }
                };
                found.sort(desc ? c.reversed() : c);
            }
            if (mm.group(2) != null) { int top = Integer.parseInt(mm.group(2)); if (found.size() > top) found = new ArrayList<>(found.subList(0, top)); }
            switch (kind) {
                case "exists": return !found.isEmpty();
                case "count": return (long) found.size();
                case "delete": for (Object o : found) rows.remove(idField.get(o)); return null;
                default:
                    if (m.getReturnType() == Optional.class) return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
                    return found;
            }
        }

        private static Object get(Object row, String prop) throws Exception {
            String cap = Character.toUpperCase(prop.charAt(0)) + prop.substring(1);
            for (String prefix : new String[] { "get", "is" }) {
                try { return row.getClass().getMethod(prefix + cap).invoke(row); } catch (NoSuchMethodException e) { /* try next */ }
            }
            throw new IllegalStateException("No property " + prop + " on " + row.getClass().getSimpleName());
        }
    }
}
