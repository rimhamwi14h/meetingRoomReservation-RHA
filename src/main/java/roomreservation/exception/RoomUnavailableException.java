package roomreservation.exception;

public class RoomUnavailableException extends RuntimeException {

    public RoomUnavailableException(Long id) {
        super("Room " + id + " is unavailable");
    }
}