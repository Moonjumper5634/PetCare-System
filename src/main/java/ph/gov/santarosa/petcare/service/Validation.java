package ph.gov.santarosa.petcare.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.regex.Pattern;

/** Server-side input checks. The browser's own checks are a convenience; these are the real ones. */
public final class Validation {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+()\\-\\s]{7,30}$");
    private static final Pattern MICROCHIP = Pattern.compile("^[A-Za-z0-9\\-]{1,40}$");

    private Validation() { }

    /** Trimmed text; blank becomes null. Enforces required and max length. */
    public static String text(String v, String label, int max, boolean required) {
        String t = v == null ? null : v.trim();
        if (t == null || t.isEmpty()) {
            if (required) throw ApiException.badRequest(label + " is required.");
            return null;
        }
        if (t.length() > max) throw ApiException.badRequest(label + " must be at most " + max + " characters.");
        return t;
    }

    public static String oneOf(String v, String label, Collection<String> allowed, boolean required) {
        String t = text(v, label, 80, required);
        if (t != null && !allowed.contains(t)) throw ApiException.badRequest(label + " has an invalid value.");
        return t;
    }

    public static LocalDate date(String v, String label, boolean required) {
        String t = text(v, label, 10, required);
        if (t == null) return null;
        try { return LocalDate.parse(t); }
        catch (DateTimeParseException e) { throw ApiException.badRequest(label + " is not a valid date."); }
    }

    public static LocalDate pastOrToday(String v, String label, boolean required, LocalDate today) {
        LocalDate d = date(v, label, required);
        if (d != null && d.isAfter(today)) throw ApiException.badRequest(label + " cannot be in the future.");
        return d;
    }

    public static String email(String v, boolean required) {
        String t = text(v, "Email", 190, required);
        if (t != null && !EMAIL.matcher(t).matches()) throw ApiException.badRequest("Invalid email.");
        return t == null ? null : t.toLowerCase();
    }

    public static String phone(String v, boolean required) {
        String t = text(v, "Mobile number", 30, required);
        if (t != null && !PHONE.matcher(t).matches()) throw ApiException.badRequest("Mobile number is not valid.");
        return t;
    }

    public static String microchip(String v) {
        String t = text(v, "Microchip number", 40, false);
        if (t != null && !MICROCHIP.matcher(t).matches())
            throw ApiException.badRequest("Microchip number may contain only letters, digits and dashes.");
        return t;
    }

    /** 8-72 characters with at least one letter and one digit (72 is bcrypt's limit). */
    public static void password(String pw) {
        if (pw == null || pw.length() < 8) throw ApiException.badRequest("Password must be at least 8 characters.");
        if (pw.length() > 72) throw ApiException.badRequest("Password must be at most 72 characters.");
        boolean letter = false, digit = false;
        for (char c : pw.toCharArray()) { if (Character.isLetter(c)) letter = true; if (Character.isDigit(c)) digit = true; }
        if (!letter || !digit) throw ApiException.badRequest("Password must contain at least one letter and one number.");
    }
}
