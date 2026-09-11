package roomreservation.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import roomreservation.model.Building;
import roomreservation.service.BuildingService;
import java.util.List;
@RestController
@RequestMapping("/api/buildings")
public class BuildingController {
    @Autowired
    private BuildingService buildingService;
    @PostMapping
    public Building createBuilding(@RequestBody Building building){
        return buildingService.createBuilding(building);
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
            @RequestBody Building building){
        return buildingService.updateBuilding(id, building);
    }
}
