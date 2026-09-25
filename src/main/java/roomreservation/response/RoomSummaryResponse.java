package roomreservation.response;

import lombok.Data;
import roomreservation.model.Building;
import roomreservation.model.RoomStatus;

@Data
public class RoomSummaryResponse {

    private Long id;
    private String name;
    private Building building;
    private Integer floor;
    private Integer capacity;
    private RoomStatus status;
}