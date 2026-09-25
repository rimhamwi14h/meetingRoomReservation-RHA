package roomreservation.exception;

public class RoomCapacityExceededException extends RuntimeException {

    private final Long roomId;
    private final Integer roomCapacity;
    private final Integer numberOfParticipants;

    public RoomCapacityExceededException(
            Long roomId,
            Integer roomCapacity,
            Integer numberOfParticipants) {

        super("Room capacity is insufficient");

        this.roomId = roomId;
        this.roomCapacity = roomCapacity;
        this.numberOfParticipants = numberOfParticipants;
    }

    public Long getRoomId() {
        return roomId;
    }

    public Integer getRoomCapacity() {
        return roomCapacity;
    }

    public Integer getNumberOfParticipants() {
        return numberOfParticipants;
    }
}