package roomreservation.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import roomreservation.model.Building;
import roomreservation.service.BuildingService;
import java.util.List;
import jakarta.validation.Valid;
import roomreservation.request.CreateBuildingRequest;
import roomreservation.request.UpdateBuildingRequest;
@RestController
@RequestMapping("/api/buildings")
public class BuildingController {
    @Autowired
    private BuildingService buildingService;
    @PostMapping
    public Building createBuilding(@Valid  @RequestBody CreateBuildingRequest request){
        return buildingService.createBuilding(request);
    }
    @GetMapping
    public List<Building> getAllBuildings(){
        return buildingService.getAllBuildings();
    }
    @GetMapping("/{id}")
    public Building getBuilding (@PathVariable Long id) {
        return buildingService.getBuilding (id);
    }
    @PutMapping("/{id}")
    public Building updateBuilding(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBuildingRequest request) {

        return buildingService.updateBuilding(id, request);
    }
}
