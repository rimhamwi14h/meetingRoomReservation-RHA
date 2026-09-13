package roomreservation.exception;

public class BuildingNotFoundException extends RuntimeException {

    public BuildingNotFoundException(Long id) {
        super("Building " + id + " not found");
    }
}