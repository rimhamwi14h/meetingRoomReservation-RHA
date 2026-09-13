package roomreservation.exception;

public class EquipmentNotFoundException extends RuntimeException {

    public EquipmentNotFoundException(String code) {
        super("Equipment " + code + " not found");
    }
}