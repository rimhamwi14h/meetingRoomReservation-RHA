package roomreservation.exception;

public class ReservationNotFoundException
        extends RuntimeException {

    private final Long reservationId;

    public ReservationNotFoundException(Long reservationId) {

        super(
                "Reservation "
                        + reservationId
                        + " not found"
        );

        this.reservationId = reservationId;
    }

    public Long getReservationId() {
        return reservationId;
    }
}