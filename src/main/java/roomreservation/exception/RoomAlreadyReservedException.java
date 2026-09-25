package roomreservation.exception;

public class RoomAlreadyReservedException
        extends RuntimeException {

    private final Long roomId;
    private final Long conflictingReservationId;

    public RoomAlreadyReservedException(
            Long roomId,
            Long conflictingReservationId) {

        super(
                "Room is already reserved during this period"
        );

        this.roomId = roomId;
        this.conflictingReservationId =
                conflictingReservationId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public Long getConflictingReservationId() {
        return conflictingReservationId;
    }
}