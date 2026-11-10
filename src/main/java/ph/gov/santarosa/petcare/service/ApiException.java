package ph.gov.santarosa.petcare.service;

/** An expected, user-facing failure. The web layer turns it into {"error": message} with the given HTTP status. */
public class ApiException extends RuntimeException {

    private final int status;
    private final String code;

    public ApiException(int status, String message) { this(status, null, message); }

    public ApiException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public int getStatus() { return status; }
    public String getCode() { return code; }

    public static ApiException badRequest(String m) { return new ApiException(400, m); }
    public static ApiException unauthorized(String m) { return new ApiException(401, m); }
    public static ApiException forbidden(String m) { return new ApiException(403, m); }
    public static ApiException notFound(String m) { return new ApiException(404, m); }
    public static ApiException conflict(String m) { return new ApiException(409, m); }
    public static ApiException serviceUnavailable(String m) { return new ApiException(503, m); }
}
