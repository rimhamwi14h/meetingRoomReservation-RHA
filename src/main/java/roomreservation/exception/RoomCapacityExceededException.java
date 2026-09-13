package roomreservation.exception;

public class RoomCapacityExceededException extends RuntimeException {

    public RoomCapacityExceededException(Long roomId) {
        super("Room " + roomId + " does not have enough capacity");
    }
}