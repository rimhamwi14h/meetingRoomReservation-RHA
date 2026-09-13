package roomreservation.exception;

public class RoomAlreadyReservedException extends RuntimeException {

    public RoomAlreadyReservedException(Long roomId) {
        super("Room " + roomId + " is already reserved for this period");
    }
}