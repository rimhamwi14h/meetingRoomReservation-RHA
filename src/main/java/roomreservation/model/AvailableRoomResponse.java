package roomreservation.model;
import lombok.Data;
import java.util.Set;
@Data
public class AvailableRoomResponse {
    private Long id;
    private String name;
    private Building building;
    private Integer floor;
    private Integer capacity;
    private RoomStatus status;
    private Set<Equipment> equipment;
    private Integer unusedCapacity;
}
