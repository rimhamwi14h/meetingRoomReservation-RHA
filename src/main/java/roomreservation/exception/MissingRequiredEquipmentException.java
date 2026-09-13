package roomreservation.exception;

public class MissingRequiredEquipmentException extends RuntimeException {

    public MissingRequiredEquipmentException(String code) {
        super("Room does not contain required equipment " + code);
    }
}