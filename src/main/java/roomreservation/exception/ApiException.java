package roomreservation.exception;
import org.springframework.http.HttpStatus;
public class ApiException extends RuntimeException {
    private String code;
    private HttpStatus status;
    public ApiException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }
    public String getCode(){
        return code;
    }
    public HttpStatus getStatus(){
        return status;
    }
}
