package roomreservation.exception;

public class EquipmentNotFoundException extends RuntimeException {

    private final String equipmentCode;

    public EquipmentNotFoundException(String equipmentCode) {
        super("Equipment " + equipmentCode + " not found");
        this.equipmentCode = equipmentCode;
    }

    public String getEquipmentCode() {
        return equipmentCode;
    }
}