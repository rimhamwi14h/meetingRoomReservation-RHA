package roomreservation.exception;

public class OrganizerNotFoundException extends RuntimeException {

    private final Long organizerId;

    public OrganizerNotFoundException(Long organizerId) {
        super("Organizer " + organizerId + " not found");
        this.organizerId = organizerId;
    }

    public Long getOrganizerId() {
        return organizerId;
    }
}