package roomreservation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import roomreservation.exception.BuildingNotFoundException;
import roomreservation.exception.InvalidFloorException;
import roomreservation.exception.OrganizerNotFoundException;
import roomreservation.exception.ResourceAlreadyExistsException;

import roomreservation.model.Building;
import roomreservation.model.Organizer;

import roomreservation.repository.BuildingRepository;
import roomreservation.repository.OrganizerRepository;

import roomreservation.request.CreateOrganizerRequest;

import java.util.Comparator;
import java.util.List;


/**
 * Service responsible for organizer management.
 *
 * It handles organizer creation, validation,
 * retrieval and sorting.
 */
@Service
public class OrganizerService {

    @Autowired
    private OrganizerRepository organizerRepository;

    @Autowired
    private BuildingRepository buildingRepository;


    /**
     * Creates a new organizer.
     *
     * The method checks that the email is unique,
     * that the referenced building exists and that
     * the requested floor belongs to this building.
     *
     * @param request organizer creation information
     * @return the created organizer
     */
    public Organizer createOrganizer(
            CreateOrganizerRequest request) {

        if (organizerRepository.existsByEmail(
                request.getEmail())) {

            throw new ResourceAlreadyExistsException(
                    "An organizer already uses this email"
            );
        }

        Building building =
                buildingRepository
                        .findById(
                                request.getBuildingId()
                        )
                        .orElseThrow(() ->
                                new BuildingNotFoundException(
                                        request.getBuildingId()
                                )
                        );

        if (request.getFloor()
                >= building.getNumberOfFloors()) {

            throw new InvalidFloorException(
                    request.getFloor(),
                    building.getId()
            );
        }

        Organizer organizer =
                new Organizer();

        organizer.setName(
                request.getName()
        );

        organizer.setEmail(
                request.getEmail()
        );

        organizer.setBuilding(
                building
        );

        organizer.setFloor(
                request.getFloor()
        );

        return organizerRepository.save(
                organizer
        );
    }


    /**
     * Returns an organizer by its identifier.
     *
     * @param id organizer identifier
     * @return the requested organizer
     */
    public Organizer getOrganizer(Long id) {

        return organizerRepository
                .findById(id)
                .orElseThrow(() ->
                        new OrganizerNotFoundException(id)
                );
    }


    /**
     * Returns all organizers sorted by name,
     * then by identifier.
     *
     * The name comparison does not take
     * letter case into account.
     *
     * @return sorted list of organizers
     */
    public List<Organizer> getAllOrganizers() {

        List<Organizer> organizers =
                organizerRepository.findAll();

        organizers.sort(
                Comparator.comparing(
                        Organizer::getName,
                        String.CASE_INSENSITIVE_ORDER
                ).thenComparing(
                        Organizer::getId
                )
        );

        return organizers;
    }
}