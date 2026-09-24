package roomreservation.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateRoomRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private Long buildingId;

    @NotNull
    @Min(0)
    @Max(199)
    private Integer floor;

    @NotNull
    @Min(1)
    private Integer capacity;
}