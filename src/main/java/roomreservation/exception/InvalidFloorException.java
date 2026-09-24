package roomreservation.exception;

public class InvalidFloorException extends RuntimeException {

    public InvalidFloorException(Integer floor, Long buildingId) {
        super("Floor " + floor + " does not exist in building " + buildingId);
    }
}