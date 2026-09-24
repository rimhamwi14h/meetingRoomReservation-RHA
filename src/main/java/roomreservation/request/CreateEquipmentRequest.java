package roomreservation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateEquipmentRequest {

    @NotBlank
    @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,49}$")
    private String code;

    @NotBlank
    @Size(max = 100)
    private String label;
}