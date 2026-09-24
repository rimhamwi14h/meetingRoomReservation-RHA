package roomreservation.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import roomreservation.model.RoomStatus;

@Data
public class UpdateRoomStatusRequest {

    @NotNull
    private RoomStatus status;
}