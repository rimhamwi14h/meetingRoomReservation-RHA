package roomreservation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import roomreservation.exception.EquipmentNotFoundException;
import roomreservation.exception.InvalidReservationPeriodException;
import roomreservation.exception.MissingRequiredEquipmentException;
import roomreservation.exception.NoCompatibleRoomException;
import roomreservation.exception.OrganizerNotFoundException;
import roomreservation.exception.ReservationAlreadyCancelledException;
import roomreservation.exception.ReservationNotFoundException;
import roomreservation.exception.RoomAlreadyReservedException;
import roomreservation.exception.RoomCapacityExceededException;
import roomreservation.exception.RoomNotFoundException;
import roomreservation.exception.RoomUnavailableException;

import roomreservation.model.*;
import roomreservation.repository.EquipmentRepository;
import roomreservation.repository.OrganizerRepository;
import roomreservation.repository.ReservationRepository;
import roomreservation.repository.RoomRepository;

import roomreservation.request.AutomaticReservationRequest;
import roomreservation.request.CreateReservationRequest;

import roomreservation.response.OrganizerSummaryResponse;
import roomreservation.response.ReservationResponse;
import roomreservation.response.RoomSummaryResponse;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * Service responsible for reservation management.
 *
 * It handles manual and automatic room reservations,
 * conflict detection, room scoring and reservation cancellation.
 */
@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private OrganizerRepository organizerRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    public ReservationResponse createReservation(
            CreateReservationRequest request) {

        Room room =
                roomRepository
                        .findById(request.getRoomId())
                        .orElseThrow(() ->
                                new RoomNotFoundException(
                                        request.getRoomId()
                                )
                        );

        Organizer organizer =
                organizerRepository
                        .findById(request.getOrganizerId())
                        .orElseThrow(() ->
                                new OrganizerNotFoundException(
                                        request.getOrganizerId()
                                )
                        );


        if (!request.getStart()
                .isBefore(request.getEnd())) {

            throw new InvalidReservationPeriodException(
                    "Start must be before end"
            );
        }


        if (request.getStart()
                .isBefore(OffsetDateTime.now())) {

            throw new InvalidReservationPeriodException(
                    "Start cannot be in the past"
            );
        }


        if (Duration.between(
                request.getStart(),
                request.getEnd()
        ).compareTo(Duration.ofHours(8)) > 0) {

            throw new InvalidReservationPeriodException(
                    "Reservation cannot exceed 8 hours"
            );
        }


        if (room.getStatus()
                != RoomStatus.AVAILABLE) {

            throw new RoomUnavailableException(
                    room.getId()
            );
        }


        if (room.getCapacity()
                < request.getNumberOfParticipants()) {

            throw new RoomCapacityExceededException(
                    room.getId(),
                    room.getCapacity(),
                    request.getNumberOfParticipants()
            );
        }


        Set<Equipment> requiredEquipment =
                new HashSet<>();

        Set<String> missingEquipmentCodes =
                new HashSet<>();


        if (request.getRequiredEquipmentCodes()
                != null) {

            for (String code :
                    request.getRequiredEquipmentCodes()) {

                Equipment equipment =
                        equipmentRepository
                                .findByCode(code);

                if (equipment == null) {

                    throw new EquipmentNotFoundException(
                            code
                    );
                }

                requiredEquipment.add(equipment);

                boolean foundInRoom = false;

                for (Equipment roomEquipment :
                        room.getEquipment()) {

                    if (roomEquipment
                            .getCode()
                            .equals(code)) {

                        foundInRoom = true;
                        break;
                    }
                }

                if (!foundInRoom) {
                    missingEquipmentCodes.add(code);
                }
            }
        }


        if (!missingEquipmentCodes.isEmpty()) {

            throw new MissingRequiredEquipmentException(
                    room.getId(),
                    missingEquipmentCodes
            );
        }


        Reservation conflictingReservation =
                reservationRepository
                        .findFirstByRoomAndStatusAndStartLessThanAndEndGreaterThan(
                                room,
                                ReservationStatus.CONFIRMED,
                                request.getEnd(),
                                request.getStart()
                        )
                        .orElse(null);


        if (conflictingReservation != null) {

            throw new RoomAlreadyReservedException(
                    room.getId(),
                    conflictingReservation.getId()
            );
        }


        Reservation reservation =
                new Reservation();

        reservation.setTitle(
                request.getTitle()
        );

        reservation.setRoom(
                room
        );

        reservation.setOrganizer(
                organizer
        );

        reservation.setStart(
                request.getStart()
        );

        reservation.setEnd(
                request.getEnd()
        );

        reservation.setNumberOfParticipants(
                request.getNumberOfParticipants()
        );

        reservation.setStatus(
                ReservationStatus.CONFIRMED
        );

        reservation.setRequiredEquipment(
                requiredEquipment
        );


        Reservation savedReservation =
                reservationRepository
                        .save(reservation);


        return toResponse(savedReservation);
    }
    public ReservationResponse cancelReservation(
            Long id) {

        Reservation reservation =
                reservationRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ReservationNotFoundException(id)
                        );


        if (reservation.getStatus()
                == ReservationStatus.CANCELLED) {

            throw new ReservationAlreadyCancelledException(
                    id
            );
        }


        reservation.setStatus(
                ReservationStatus.CANCELLED
        );


        Reservation savedReservation =
                reservationRepository
                        .save(reservation);


        return toResponse(savedReservation);
    }
    public ReservationResponse getReservation(Long id) {

        Reservation reservation =
                reservationRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ReservationNotFoundException(id)
                        );

        return toResponse(reservation);
    }

    public List<ReservationResponse> getReservations(
            Long roomId,
            Long organizerId,
            OffsetDateTime from,
            OffsetDateTime to) {
        if (from != null &&
                to != null &&
                !from.isBefore(to)) {

            throw new InvalidReservationPeriodException(
                    "From must be before to"
            );
        }

        List<Reservation> reservations =
                reservationRepository.findAll();

        List<Reservation> filteredReservations =
                new ArrayList<>();

        for (Reservation reservation : reservations) {

            if (roomId != null &&
                    !reservation.getRoom()
                            .getId()
                            .equals(roomId)) {
                continue;
            }

            if (organizerId != null &&
                    !reservation.getOrganizer()
                            .getId()
                            .equals(organizerId)) {
                continue;
            }

            if (from != null &&
                    !reservation.getEnd().isAfter(from)) {
                continue;
            }

            if (to != null &&
                    !reservation.getStart().isBefore(to)) {
                continue;
            }

            filteredReservations.add(reservation);
        }

        filteredReservations.sort(
                Comparator
                        .comparing(Reservation::getStart)
                        .thenComparing(Reservation::getId)
        );

        List<ReservationResponse> responses =
                new ArrayList<>();

        for (Reservation reservation :
                filteredReservations) {

            responses.add(
                    toResponse(reservation)
            );
        }

        return responses;
    }
    /**
     * Calculates the distance between a room and an organizer.
     *
     * Rooms in the same building use the floor difference.
     * Rooms in different buildings receive an additional penalty of 10.
     *
     * @param room the room
     * @param organizer the organizer
     * @return the calculated distance
     */
    public int calculateDistance(
            Room room,
            Organizer organizer) {

        int floorDistance = Math.abs(
                room.getFloor() - organizer.getFloor()
        );

        if (room.getBuilding()
                .getId()
                .equals(
                        organizer.getBuilding().getId()
                )) {

            return floorDistance;
        }

        return 10 + floorDistance;
    }
    /**
     * Calculates the score used for automatic room assignment.
     *
     * The score combines the distance and the unused room capacity.
     *
     * @param room the candidate room
     * @param organizer the organizer
     * @param numberOfParticipants number of participants
     * @return the room assignment score
     */
    public long calculateScore(
            Room room,
            Organizer organizer,
            Integer numberOfParticipants) {

        int distance =
                calculateDistance(room, organizer);

        int unusedCapacity =
                room.getCapacity()
                        - numberOfParticipants;

        return distance * 10L + unusedCapacity;
    }
    /**
     * Creates a reservation by automatically selecting
     * the most suitable compatible room.
     *
     * @param request reservation information
     * @return the created reservation
     */
    public ReservationResponse createAutomaticReservation(
            AutomaticReservationRequest request) {
        Organizer organizer =
                organizerRepository
                        .findById(request.getOrganizerId())
                        .orElseThrow(() ->
                                new OrganizerNotFoundException(
                                        request.getOrganizerId()
                                )
                        );

        if (!request.getStart().isBefore(request.getEnd())) {
            throw new InvalidReservationPeriodException(
                    "Start must be before end"
            );
        }

        if (request.getStart().isBefore(OffsetDateTime.now())) {
            throw new InvalidReservationPeriodException(
                    "Start cannot be in the past"
            );
        }

        if (Duration.between(
                request.getStart(),
                request.getEnd()
        ).compareTo(Duration.ofHours(8)) > 0) {

            throw new InvalidReservationPeriodException(
                    "Reservation cannot exceed 8 hours"
            );
        }

        Set<Equipment> requiredEquipment =
                new HashSet<>();

        if (request.getRequiredEquipmentCodes() != null) {

            for (String code :
                    request.getRequiredEquipmentCodes()) {

                Equipment equipment =
                        equipmentRepository.findByCode(code);

                if (equipment == null) {
                    throw new EquipmentNotFoundException(
                            code
                    );
                }

                requiredEquipment.add(equipment);
            }
        }

        List<Room> rooms =
                roomRepository.findAll();

        Room bestRoom = null;
        long bestScore = Long.MAX_VALUE;

        for (Room room : rooms) {

            if (room.getStatus()
                    != RoomStatus.AVAILABLE) {

                continue;
            }

            if (room.getCapacity()
                    < request.getNumberOfParticipants()) {

                continue;
            }

            boolean hasAllEquipment = true;

            Set<String> requestedCodes =
                    request.getRequiredEquipmentCodes()
                            == null
                            ? new HashSet<>()
                            : request.getRequiredEquipmentCodes();

            for (String code : requestedCodes) {

                boolean found = false;

                for (Equipment equipment :
                        room.getEquipment()) {

                    if (equipment.getCode().equals(code)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    hasAllEquipment = false;
                    break;
                }
            }

            if (!hasAllEquipment) {
                continue;
            }

            boolean conflict =
                    reservationRepository
                            .existsByRoomAndStatusAndStartLessThanAndEndGreaterThan(
                                    room,
                                    ReservationStatus.CONFIRMED,
                                    request.getEnd(),
                                    request.getStart()
                            );

            if (conflict) {
                continue;
            }

            long score = calculateScore(
                    room,
                    organizer,
                    request.getNumberOfParticipants()
            );

            if (bestRoom == null ||
                    score < bestScore) {

                bestRoom = room;
                bestScore = score;

            } else if (score == bestScore) {

                int nameComparison =
                        room.getName()
                                .compareToIgnoreCase(
                                        bestRoom.getName()
                                );

                if (nameComparison < 0) {

                    bestRoom = room;

                } else if (
                        nameComparison == 0 &&
                                room.getId()
                                        < bestRoom.getId()) {

                    bestRoom = room;
                }
            }
        }

        if (bestRoom == null) {
            throw new NoCompatibleRoomException();
        }

        Reservation reservation = new Reservation();

        reservation.setTitle(request.getTitle());
        reservation.setOrganizer(organizer);
        reservation.setRoom(bestRoom);
        reservation.setStart(request.getStart());
        reservation.setEnd(request.getEnd());

        reservation.setNumberOfParticipants(
                request.getNumberOfParticipants()
        );

        reservation.setStatus(
                ReservationStatus.CONFIRMED
        );

        reservation.setRequiredEquipment(
                requiredEquipment
        );

        Reservation savedReservation =
                reservationRepository.save(reservation);

        return toResponse(savedReservation);    }

    private ReservationResponse toResponse(
            Reservation reservation) {

        ReservationResponse response =
                new ReservationResponse();

        response.setId(reservation.getId());
        response.setTitle(reservation.getTitle());
        response.setStatus(reservation.getStatus());
        response.setStart(reservation.getStart());
        response.setEnd(reservation.getEnd());

        response.setNumberOfParticipants(
                reservation.getNumberOfParticipants()
        );

        response.setCreatedAt(
                reservation.getCreatedAt()
        );

        RoomSummaryResponse roomResponse =
                new RoomSummaryResponse();

        roomResponse.setId(
                reservation.getRoom().getId()
        );

        roomResponse.setName(
                reservation.getRoom().getName()
        );

        roomResponse.setBuilding(
                reservation.getRoom().getBuilding()
        );

        roomResponse.setFloor(
                reservation.getRoom().getFloor()
        );

        roomResponse.setCapacity(
                reservation.getRoom().getCapacity()
        );

        roomResponse.setStatus(
                reservation.getRoom().getStatus()
        );

        response.setRoom(roomResponse);

        OrganizerSummaryResponse organizerResponse =
                new OrganizerSummaryResponse();

        organizerResponse.setId(
                reservation.getOrganizer().getId()
        );

        organizerResponse.setName(
                reservation.getOrganizer().getName()
        );

        organizerResponse.setBuilding(
                reservation.getOrganizer().getBuilding()
        );

        organizerResponse.setFloor(
                reservation.getOrganizer().getFloor()
        );

        response.setOrganizer(
                organizerResponse
        );

        Set<String> equipmentCodes =
                new HashSet<>();

        for (Equipment equipment :
                reservation.getRequiredEquipment()) {

            equipmentCodes.add(
                    equipment.getCode()
            );
        }

        response.setRequiredEquipmentCodes(
                equipmentCodes
        );

        return response;
    }
}