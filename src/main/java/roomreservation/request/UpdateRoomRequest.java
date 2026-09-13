package roomreservation.request;
import lombok.Data;
@Data
public class UpdateRoomRequest {
    private String name;
    private Long buildingId;
    private Integer floor;
    private Integer capacity;
}
