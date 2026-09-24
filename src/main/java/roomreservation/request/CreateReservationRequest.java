package roomreservation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
public class CreateReservationRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotNull
    private Long organizerId;

    @NotNull
    private Long roomId;

    @NotNull
    private OffsetDateTime start;

    @NotNull
    private OffsetDateTime end;

    @NotNull
    @Positive
    private Integer numberOfParticipants;

    private Set<
            @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,49}$")
                    String
            > requiredEquipmentCodes;
}