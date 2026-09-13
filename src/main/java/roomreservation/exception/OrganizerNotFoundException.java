package roomreservation.exception;

public class OrganizerNotFoundException extends RuntimeException {

    public OrganizerNotFoundException(Long id) {
        super("Organizer " + id + " not found");
    }
}