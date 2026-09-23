package roomreservation.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class UpdateBuildingRequest {
    @NotBlank
    @Size(max = 100)
    private String name;
    @NotNull
    @Min(1)
    @Max(200)
    private Integer numberOfFloors;
}