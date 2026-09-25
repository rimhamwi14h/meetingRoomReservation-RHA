package roomreservation.exception;

public class RoomNotFoundException extends RuntimeException {

    private final Long roomId;

    public RoomNotFoundException(Long roomId) {
        super("Room " + roomId + " not found");
        this.roomId = roomId;
    }

    public Long getRoomId() {
        return roomId;
    }
}