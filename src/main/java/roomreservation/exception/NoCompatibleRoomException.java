package roomreservation.exception;

public class NoCompatibleRoomException extends RuntimeException {

    public NoCompatibleRoomException() {
        super("No compatible room found");
    }
}