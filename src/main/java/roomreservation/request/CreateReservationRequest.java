package roomreservation.request;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.Set;
@Data
public class CreateReservationRequest {
    private String title;
    private Long organizerId;
    private Long roomId;
    private OffsetDateTime start;
    private OffsetDateTime end;
    private Integer numberOfParticipants;
    private Set<String> requiredEquipmentCodes;
}