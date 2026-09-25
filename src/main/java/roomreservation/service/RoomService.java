package roomreservation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import roomreservation.exception.BuildingNotFoundException;
import roomreservation.exception.EquipmentNotFoundException;
import roomreservation.exception.InvalidFloorException;
import roomreservation.exception.InvalidReservationPeriodException;
import roomreservation.exception.ResourceAlreadyExistsException;
import roomreservation.exception.RoomNotFoundException;

import roomreservation.model.*;

import roomreservation.repository.BuildingRepository;
import roomreservation.repository.EquipmentRepository;
import roomreservation.repository.ReservationRepository;
import roomreservation.repository.RoomRepository;

import roomreservation.request.CreateRoomRequest;
import roomreservation.request.ReplaceRoomEquipmentRequest;
import roomreservation.request.UpdateRoomRequest;
import roomreservation.request.UpdateRoomStatusRequest;

import java.time.OffsetDateTime;
import java.util.*;


/**
 * Service responsible for room management.
 *
 * It handles room creation, retrieval, update,
 * status changes, equipment replacement and
 * room availability searches.
 */
@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;


    /**
     * Creates a new room.
     *
     * The method validates the building,
     * the floor, the room name and the requested equipment.
     * A newly created room is AVAILABLE by default.
     *
     * @param request room creation information
     * @return the created room
     */
    public Room createRoom(CreateRoomRequest request) {

        Building building = buildingRepository
                .findById(request.getBuildingId())
                .orElseThrow(() ->
                        new BuildingNotFoundException(
                                request.getBuildingId()
                        ));

        if (roomRepository.existsByNameIgnoreCase(
                request.getName())) {

            throw new ResourceAlreadyExistsException(
                    "A room already uses this name"
            );
        }

        if (request.getFloor()
                >= building.getNumberOfFloors()) {

            throw new InvalidFloorException(
                    request.getFloor(),
                    building.getId()
            );
        }

        Set<Equipment> equipment =
                new HashSet<>();

        if (request.getEquipmentCodes() != null) {

            for (String code :
                    request.getEquipmentCodes()) {

                Equipment e =
                        equipmentRepository.findByCode(code);

                if (e == null) {

                    throw new EquipmentNotFoundException(
                            code
                    );
                }

                equipment.add(e);
            }
        }

        Room room = new Room();

        room.setName(
                request.getName()
        );

        room.setBuilding(
                building
        );

        room.setFloor(
                request.getFloor()
        );

        room.setCapacity(
                request.getCapacity()
        );

        room.setStatus(
                RoomStatus.AVAILABLE
        );

        room.setEquipment(
                equipment
        );

        return roomRepository.save(room);
    }


    /**
     * Returns all rooms.
     *
     * Rooms are sorted by name without taking
     * letter case into account, then by identifier.
     *
     * @return sorted list of rooms
     */
    public List<Room> getAllRooms() {

        List<Room> rooms =
                roomRepository.findAll();

        rooms.sort(
                Comparator.comparing(
                        Room::getName,
                        String.CASE_INSENSITIVE_ORDER
                ).thenComparing(
                        Room::getId
                )
        );

        return rooms;
    }


    /**
     * Returns a room by its identifier.
     *
     * @param id room identifier
     * @return the requested room
     */
    public Room getRoom(Long id) {

        return roomRepository
                .findById(id)
                .orElseThrow(() ->
                        new RoomNotFoundException(id)
                );
    }


    /**
     * Updates an existing room.
     *
     * The method validates the room name,
     * the referenced building and the requested floor.
     *
     * @param id room identifier
     * @param request updated room information
     * @return the updated room
     */
    public Room updateRoom(
            Long id,
            UpdateRoomRequest request) {

        Room room = roomRepository
                .findById(id)
                .orElseThrow(() ->
                        new RoomNotFoundException(id)
                );

        if (roomRepository
                .existsByNameIgnoreCaseAndIdNot(
                        request.getName(),
                        id
                )) {

            throw new ResourceAlreadyExistsException(
                    "A room already uses this name"
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

        room.setName(
                request.getName()
        );

        room.setBuilding(
                building
        );

        room.setFloor(
                request.getFloor()
        );

        room.setCapacity(
                request.getCapacity()
        );

        return roomRepository.save(room);
    }


    /**
     * Changes the status of a room.
     *
     * A room can for example be marked as AVAILABLE
     * or MAINTENANCE.
     *
     * @param id room identifier
     * @param request requested status
     * @return the updated room
     */
    public Room updateRoomStatus(
            Long id,
            UpdateRoomStatusRequest request) {

        Room room =
                roomRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RoomNotFoundException(id)
                        );

        room.setStatus(
                request.getStatus()
        );

        return roomRepository.save(room);
    }


    /**
     * Replaces all equipment associated with a room.
     *
     * Each requested equipment code must exist.
     *
     * @param id room identifier
     * @param request new equipment list
     * @return the updated room
     */
    public Room replaceRoomEquipment(
            Long id,
            ReplaceRoomEquipmentRequest request) {

        Room room =
                roomRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RoomNotFoundException(id)
                        );

        Set<Equipment> equipment =
                new HashSet<>();

        for (String code :
                request.getEquipmentCodes()) {

            Equipment e =
                    equipmentRepository.findByCode(code);

            if (e == null) {

                throw new EquipmentNotFoundException(
                        code
                );
            }

            equipment.add(e);
        }

        room.setEquipment(
                equipment
        );

        return roomRepository.save(room);
    }


    /**
     * Searches for rooms available for a requested period.
     *
     * A room is considered compatible if it is AVAILABLE,
     * has sufficient capacity, contains all requested equipment
     * and has no conflicting confirmed reservation.
     *
     * The result is sorted by unused capacity,
     * then by room name, then by identifier.
     *
     * @param start requested start date and time
     * @param end requested end date and time
     * @param capacity minimum required capacity
     * @param equipmentCodes required equipment codes
     * @return sorted list of available rooms
     */
    public List<AvailableRoomResponse> findAvailableRooms(
            OffsetDateTime start,
            OffsetDateTime end,
            Integer capacity,
            Set<String> equipmentCodes) {

        /*
         * The start of the requested period
         * must be strictly before the end.
         */
        if (!start.isBefore(end)) {

            throw new InvalidReservationPeriodException(
                    "Start must be before end"
            );
        }

        List<Room> rooms =
                roomRepository.findAll();

        List<AvailableRoomResponse> availableRooms =
                new ArrayList<>();


        for (Room room : rooms) {

            // A room in maintenance cannot be proposed.
            if (room.getStatus()
                    != RoomStatus.AVAILABLE) {

                continue;
            }

            // The room must have sufficient capacity.
            if (room.getCapacity()
                    < capacity) {

                continue;
            }


            boolean hasAllEquipment = true;


            if (equipmentCodes != null) {

                for (String code :
                        equipmentCodes) {

                    boolean found = false;

                    for (Equipment equipment :
                            room.getEquipment()) {

                        if (equipment
                                .getCode()
                                .equals(code)) {

                            found = true;
                            break;
                        }
                    }

                    if (!found) {

                        hasAllEquipment = false;
                        break;
                    }
                }
            }


            if (!hasAllEquipment) {
                continue;
            }


            /*
             * A confirmed reservation blocks the room
             * when it overlaps the requested period.
             */
            boolean conflict =
                    reservationRepository
                            .existsByRoomAndStatusAndStartLessThanAndEndGreaterThan(
                                    room,
                                    ReservationStatus.CONFIRMED,
                                    end,
                                    start
                            );


            if (conflict) {
                continue;
            }


            AvailableRoomResponse response =
                    new AvailableRoomResponse();

            response.setId(
                    room.getId()
            );

            response.setName(
                    room.getName()
            );

            response.setBuilding(
                    room.getBuilding()
            );

            response.setFloor(
                    room.getFloor()
            );

            response.setCapacity(
                    room.getCapacity()
            );

            response.setStatus(
                    room.getStatus()
            );

            response.setEquipment(
                    room.getEquipment()
            );

            response.setUnusedCapacity(
                    room.getCapacity() - capacity
            );

            availableRooms.add(response);
        }


        availableRooms.sort(
                Comparator
                        .comparing(
                                AvailableRoomResponse::getUnusedCapacity
                        )
                        .thenComparing(
                                AvailableRoomResponse::getName,
                                String.CASE_INSENSITIVE_ORDER
                        )
                        .thenComparing(
                                AvailableRoomResponse::getId
                        )
        );


        return availableRooms;
    }
}