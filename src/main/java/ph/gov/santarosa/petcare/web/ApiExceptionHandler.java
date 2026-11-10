package ph.gov.santarosa.petcare.web;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ph.gov.santarosa.petcare.service.ApiException;

/** Turns failures into {"error": "..."} JSON. Never leaks stack traces or SQL to the browser. */
@RestControllerAdvice(basePackages = "ph.gov.santarosa.petcare.web")
public class ApiExceptionHandler {

    private static final Logger LOG = Logger.getLogger(ApiExceptionHandler.class.getName());

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> api(ApiException e) {
        return body(e.getStatus(), e.getMessage(), e.getCode());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> unreadable(HttpMessageNotReadableException e) {
        return body(400, "The request could not be read.", null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> integrity(DataIntegrityViolationException e) {
        LOG.log(Level.WARNING, "Data integrity violation", e);
        return body(409, "That conflicts with existing data. Please refresh and try again.", null);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> unexpected(RuntimeException e) {
        LOG.log(Level.SEVERE, "Unexpected error", e);
        return body(500, "Something went wrong on the server. Please try again.", null);
    }

    private static ResponseEntity<Map<String, Object>> body(int status, String message, String code) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("error", message);
        if (code != null) m.put("code", code);
        return ResponseEntity.status(HttpStatus.valueOf(status)).body(m);
    }
}
