package roomreservation.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Set;

@Data
public class ReplaceRoomEquipmentRequest {

    @NotNull
    private Set<
            @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,49}$")
                    String> equipmentCodes;
}