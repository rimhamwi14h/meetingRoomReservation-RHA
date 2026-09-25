package roomreservation.exception;

public class RoomUnavailableException extends RuntimeException {

    private final Long roomId;

    public RoomUnavailableException(Long roomId) {
        super("Room " + roomId + " is unavailable");
        this.roomId = roomId;
    }

    public Long getRoomId() {
        return roomId;
    }
}