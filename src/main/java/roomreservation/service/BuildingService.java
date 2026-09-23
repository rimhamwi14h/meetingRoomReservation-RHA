package roomreservation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import roomreservation.exception.BuildingFloorCountConflictException;
import roomreservation.exception.BuildingNotFoundException;
import roomreservation.exception.ResourceAlreadyExistsException;

import roomreservation.model.Building;
import roomreservation.model.Organizer;
import roomreservation.model.Room;

import roomreservation.repository.BuildingRepository;
import roomreservation.repository.OrganizerRepository;
import roomreservation.repository.RoomRepository;

import roomreservation.request.CreateBuildingRequest;
import roomreservation.request.UpdateBuildingRequest;

import java.util.Comparator;
import java.util.List;

@Service
public class BuildingService {

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private OrganizerRepository organizerRepository;


    public Building createBuilding(CreateBuildingRequest request) {

        if (buildingRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ResourceAlreadyExistsException(
                    "A building already uses this name"
            );
        }

        Building building = new Building();

        building.setName(request.getName());
        building.setNumberOfFloors(request.getNumberOfFloors());

        return buildingRepository.save(building);
    }


    public List<Building> getAllBuildings() {

        List<Building> buildings = buildingRepository.findAll();

        buildings.sort(
                Comparator.comparing(
                        Building::getName,
                        String.CASE_INSENSITIVE_ORDER
                )
        );

        return buildings;
    }


    public Building getBuilding(Long id) {

        return buildingRepository
                .findById(id)
                .orElseThrow(() ->
                        new BuildingNotFoundException(id));
    }


    public Building updateBuilding(
            Long id,
            UpdateBuildingRequest request) {

        Building building = buildingRepository
                .findById(id)
                .orElseThrow(() ->
                        new BuildingNotFoundException(id));

        if (buildingRepository.existsByNameIgnoreCaseAndIdNot(
                request.getName(),
                id)) {

            throw new ResourceAlreadyExistsException(
                    "A building already uses this name"
            );
        }

        int highestOccupiedFloor = -1;

        for (Room room : roomRepository.findByBuildingId(id)) {

            if (room.getFloor() > highestOccupiedFloor) {
                highestOccupiedFloor = room.getFloor();
            }
        }

        for (Organizer organizer :
                organizerRepository.findByBuildingId(id)) {

            if (organizer.getFloor() > highestOccupiedFloor) {
                highestOccupiedFloor = organizer.getFloor();
            }
        }

        if (highestOccupiedFloor >= request.getNumberOfFloors()) {

            throw new BuildingFloorCountConflictException(
                    highestOccupiedFloor
            );
        }

        building.setName(request.getName());
        building.setNumberOfFloors(
                request.getNumberOfFloors()
        );

        return buildingRepository.save(building);
    }
}