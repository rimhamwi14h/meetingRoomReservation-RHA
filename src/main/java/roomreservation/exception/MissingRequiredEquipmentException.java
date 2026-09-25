package roomreservation.exception;

import java.util.Set;

public class MissingRequiredEquipmentException
        extends RuntimeException {

    private final Long roomId;
    private final Set<String> missingEquipmentCodes;

    public MissingRequiredEquipmentException(
            Long roomId,
            Set<String> missingEquipmentCodes) {

        super(
                "Room does not contain all required equipment"
        );

        this.roomId = roomId;
        this.missingEquipmentCodes =
                missingEquipmentCodes;
    }

    public Long getRoomId() {
        return roomId;
    }

    public Set<String> getMissingEquipmentCodes() {
        return missingEquipmentCodes;
    }
}