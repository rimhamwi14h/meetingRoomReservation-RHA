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


/**
 * Service responsible for building management.
 *
 * It handles building creation, update and retrieval.
 * It also validates building-related business rules,
 * such as unique names and floor constraints.
 */
@Service
public class BuildingService {

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private OrganizerRepository organizerRepository;


    /**
     * Creates a new building.
     *
     * @param request building information
     * @return the created building
     */
    public Building createBuilding(
            CreateBuildingRequest request) {

        if (buildingRepository.existsByNameIgnoreCase(
                request.getName())) {

            throw new ResourceAlreadyExistsException(
                    "A building already uses this name"
            );
        }

        Building building = new Building();

        building.setName(
                request.getName()
        );

        building.setNumberOfFloors(
                request.getNumberOfFloors()
        );

        return buildingRepository.save(
                building
        );
    }


    /**
     * Returns all buildings sorted by name.
     *
     * @return list of buildings
     */
    public List<Building> getAllBuildings() {

        List<Building> buildings =
                buildingRepository.findAll();

        buildings.sort(
                Comparator.comparing(
                        Building::getName,
                        String.CASE_INSENSITIVE_ORDER
                )
        );

        return buildings;
    }


    /**
     * Returns a building by its identifier.
     *
     * @param id building identifier
     * @return the requested building
     */
    public Building getBuilding(Long id) {

        return buildingRepository
                .findById(id)
                .orElseThrow(() ->
                        new BuildingNotFoundException(id)
                );
    }


    /**
     * Updates an existing building.
     *
     * The update is refused if the new building name
     * already exists or if the number of floors becomes
     * incompatible with existing rooms or organizers.
     *
     * @param id building identifier
     * @param request updated building information
     * @return the updated building
     */
    public Building updateBuilding(
            Long id,
            UpdateBuildingRequest request) {

        Building building =
                buildingRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new BuildingNotFoundException(id)
                        );

        if (buildingRepository
                .existsByNameIgnoreCaseAndIdNot(
                        request.getName(),
                        id
                )) {

            throw new ResourceAlreadyExistsException(
                    "A building already uses this name"
            );
        }


        int highestOccupiedFloor = -1;


        for (Room room :
                roomRepository.findByBuildingId(id)) {

            if (room.getFloor()
                    > highestOccupiedFloor) {

                highestOccupiedFloor =
                        room.getFloor();
            }
        }


        for (Organizer organizer :
                organizerRepository
                        .findByBuildingId(id)) {

            if (organizer.getFloor()
                    > highestOccupiedFloor) {

                highestOccupiedFloor =
                        organizer.getFloor();
            }
        }


        if (highestOccupiedFloor
                >= request.getNumberOfFloors()) {

            throw new BuildingFloorCountConflictException(
                    highestOccupiedFloor
            );
        }


        building.setName(
                request.getName()
        );

        building.setNumberOfFloors(
                request.getNumberOfFloors()
        );


        return buildingRepository.save(
                building
        );
    }
}