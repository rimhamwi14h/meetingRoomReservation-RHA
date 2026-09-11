package roomreservation.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import roomreservation.model.Building;
import roomreservation.repository.BuildingRepository;

import java.util.Comparator;
import java.util.List;

@Service
public class BuildingService {
    @Autowired
    private BuildingRepository buildingRepository;
    public Building createBuilding(Building building){
        return buildingRepository.save(building);
    }
    public List<Building> getAllBuildings(){
        List<Building> buildings =buildingRepository.findAll();
        buildings.sort(
                Comparator.comparing(
                        Building::getName,
                        String.CASE_INSENSITIVE_ORDER
                )
        );
        return buildings;
    }
    public Building getBuilding (Long id){
        return buildingRepository.findById(id).orElse(null);
    }
    public Building updateBuilding(Long id, Building newBuilding){
        Building building = buildingRepository.findById(id).orElse(null);
        if (building == null){
            return null;
        }
        building.setName(newBuilding.getName());
        building.setNumberOfFloors(newBuilding.getNumberOfFloors());
        return buildingRepository.save(building);
    }
}
