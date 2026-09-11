package roomreservation.model;
import lombok.Data;
import java.util.Set;
@Data
public class CreateRoomRequest {
    private String name;
    private Long buildingId;
    private Integer floor;
    private Integer capacity;
    private Set<String> equipmentCodes;
}
