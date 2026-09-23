package roomreservation.response;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class ApiErrorResponse {

    private String code;
    private String message;
    private OffsetDateTime timestamp;
    private String path;

    private Map<String, Object> details = new HashMap<>();
    private Map<String, String> fieldErrors = new HashMap<>();
}