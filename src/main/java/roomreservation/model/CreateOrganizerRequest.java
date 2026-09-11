package roomreservation.model;
import lombok.Data;
@Data
public class CreateOrganizerRequest {
    private String name;
    private String email;
    private Long buildingId;
    private Integer floor;
}
