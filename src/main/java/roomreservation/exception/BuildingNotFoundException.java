package roomreservation.exception;

public class BuildingNotFoundException extends RuntimeException {

    private final Long buildingId;

    public BuildingNotFoundException(Long buildingId) {
        super("Building " + buildingId + " not found");
        this.buildingId = buildingId;
    }

    public Long getBuildingId() {
        return buildingId;
    }
}