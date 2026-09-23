package roomreservation.exception;

public class BuildingFloorCountConflictException extends RuntimeException {

    private final Integer highestOccupiedFloor;

    public BuildingFloorCountConflictException(Integer highestOccupiedFloor) {
        super("A room or organizer occupies a floor that would be removed");
        this.highestOccupiedFloor = highestOccupiedFloor;
    }

    public Integer getHighestOccupiedFloor() {
        return highestOccupiedFloor;
    }
}