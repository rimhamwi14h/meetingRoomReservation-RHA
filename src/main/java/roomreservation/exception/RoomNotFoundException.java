package roomreservation.exception;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(Long id) {
        super("Room " + id + " not found");
    }
}
