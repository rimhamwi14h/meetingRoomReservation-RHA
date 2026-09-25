package roomreservation.response;

import lombok.Data;
import roomreservation.model.Building;

@Data
public class OrganizerSummaryResponse {

    private Long id;
    private String name;
    private Building building;
    private Integer floor;
}