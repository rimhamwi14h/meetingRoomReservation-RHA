package roomreservation.exception;

public class ReservationAlreadyCancelledException extends RuntimeException {

    public ReservationAlreadyCancelledException(Long id) {
        super("Reservation " + id + " is already cancelled");
    }
}