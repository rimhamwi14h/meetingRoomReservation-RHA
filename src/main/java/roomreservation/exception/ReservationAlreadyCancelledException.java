package roomreservation.exception;

public class ReservationAlreadyCancelledException
        extends RuntimeException {

    private final Long reservationId;

    public ReservationAlreadyCancelledException(
            Long reservationId) {

        super(
                "Reservation "
                        + reservationId
                        + " is already cancelled"
        );

        this.reservationId = reservationId;
    }

    public Long getReservationId() {
        return reservationId;
    }
}