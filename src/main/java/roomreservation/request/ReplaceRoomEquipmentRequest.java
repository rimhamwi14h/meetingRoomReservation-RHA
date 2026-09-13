package roomreservation.request;
import lombok.Data;
import java.util.Set;
@Data
public class ReplaceRoomEquipmentRequest {
    private Set<String> equipmentCodes;
}
