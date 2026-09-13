package roomreservation.request;
import lombok.Data;
import roomreservation.model.RoomStatus;

@Data
public class UpdateRoomStatusRequest {
    private RoomStatus status;
}
