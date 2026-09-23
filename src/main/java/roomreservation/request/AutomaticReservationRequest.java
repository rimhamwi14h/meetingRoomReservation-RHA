package roomreservation.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.Set;
@Data
public class AutomaticReservationRequest {
    @NotBlank
    @Size(max = 200)
    private String title;
    @NotNull
    private Long organizerId;
    @NotNull
    private OffsetDateTime start;
    @NotNull
    private OffsetDateTime end;
    @NotNull
    @Positive
    private Integer numberOfParticipants;
    private Set<String> requiredEquipmentCodes;
}