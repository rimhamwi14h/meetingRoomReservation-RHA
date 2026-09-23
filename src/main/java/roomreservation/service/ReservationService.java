package roomreservation.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import roomreservation.model.*;
import roomreservation.repository.EquipmentRepository;
import roomreservation.repository.OrganizerRepository;
import roomreservation.repository.ReservationRepository;
import roomreservation.repository.RoomRepository;
import roomreservation.request.CreateReservationRequest;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import roomreservation.request.AutomaticReservationRequest;
import roomreservation.exception.OrganizerNotFoundException;
import roomreservation.exception.ReservationNotFoundException;
import roomreservation.exception.ReservationAlreadyCancelledException;
import roomreservation.exception.RoomNotFoundException;
import roomreservation.exception.EquipmentNotFoundException;
import roomreservation.exception.RoomUnavailableException;
import roomreservation.exception.RoomCapacityExceededException;
import roomreservation.exception.MissingRequiredEquipmentException;
import roomreservation.exception.RoomAlreadyReservedException;
import roomreservation.exception.NoCompatibleRoomException;
import roomreservation.exception.InvalidReservationPeriodException;
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
    public Reservation createReservation(CreateReservationRequest request) {
        Room room = roomRepository.findById(request.getRoomId()).orElseThrow(() -> new RoomNotFoundException(request.getRoomId()));
        Organizer organizer = organizerRepository.findById(request.getOrganizerId()).orElseThrow(() -> new OrganizerNotFoundException(request.getOrganizerId()));
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
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new RoomUnavailableException(room.getId());
        }
        if (room.getCapacity() < request.getNumberOfParticipants()) {
            throw new RoomCapacityExceededException(room.getId());
        }
        Set<Equipment> requiredEquipment = new HashSet<>();
        if (request.getRequiredEquipmentCodes() != null) {
            for (String code : request.getRequiredEquipmentCodes()) {
                Equipment equipment = equipmentRepository.findByCode(code);
                if (equipment == null) {
                    throw new EquipmentNotFoundException(code);
                }
                if (!room.getEquipment().contains(equipment)) {
                    throw new MissingRequiredEquipmentException(code);
                }
                requiredEquipment.add(equipment);
            }
        }
        boolean conflict = reservationRepository.existsByRoomAndStatusAndStartLessThanAndEndGreaterThan(
                                room,
                                ReservationStatus.CONFIRMED,
                                request.getEnd(),
                                request.getStart()
                        );
        if (conflict) {
            throw new RoomAlreadyReservedException(room.getId());
        }
        Reservation reservation = new Reservation();
        reservation.setTitle(request.getTitle());
        reservation.setRoom(room);
        reservation.setOrganizer(organizer);
        reservation.setStart(request.getStart());
        reservation.setEnd(request.getEnd());
        reservation.setNumberOfParticipants(
                request.getNumberOfParticipants()
        );
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setRequiredEquipment(requiredEquipment);
        return reservationRepository.save(reservation);
    }
    public Reservation cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow(() -> new ReservationNotFoundException(id));
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ReservationAlreadyCancelledException(id);
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(reservation);
    }
    public Reservation getReservation(Long id) {return reservationRepository.findById(id).orElseThrow(() -> new ReservationNotFoundException(id));
    }    public List<Reservation> getReservations (
            Long roomId,
            Long organizerId,
            OffsetDateTime from,
            OffsetDateTime to) {
        List<Reservation> reservations = reservationRepository.findAll();
        List<Reservation> result = new ArrayList<>();
        for(Reservation reservation : reservations){
            if (roomId != null &&
                    !reservation.getRoom().getId().equals(roomId)) {
                continue;
            }
            if (organizerId != null &&
                    !reservation.getOrganizer().getId().equals(organizerId)) {
                continue;
            }
            if (from != null && !reservation.getEnd().isAfter(from)) {
                continue;
            }
            if (to != null && !reservation.getStart().isBefore(to)) {
                continue;
            }
            result.add(reservation);
        }
        result.sort(
                Comparator.comparing(Reservation::getStart).thenComparing(Reservation::getId)
        );
        return result;
    }
    public int calculateDistance (Room room, Organizer organizer){
        int floorDistance = Math.abs(room.getFloor() - organizer.getFloor());
        if(room.getBuilding().getId().equals(organizer.getBuilding().getId())){
            return floorDistance;
        }
        return 10 + floorDistance;
    }
    public long calculateScore (
            Room room,
            Organizer organizer,
            Integer numberOfParticipants){
        int distance = calculateDistance(room, organizer);
        int unusedCapacity = room.getCapacity()-numberOfParticipants;
        return distance * 10L + unusedCapacity;
    }
    public Reservation createAutomaticReservation(AutomaticReservationRequest request) {
        Organizer organizer = organizerRepository.findById(request.getOrganizerId()).orElseThrow(() -> new OrganizerNotFoundException(request.getOrganizerId()));
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
        Set<Equipment> requiredEquipment = new HashSet<>();
        if (request.getRequiredEquipmentCodes() != null) {
            for (String code : request.getRequiredEquipmentCodes()) {
                Equipment equipment = equipmentRepository.findByCode(code);
                if (equipment == null) {
                    throw new EquipmentNotFoundException(code);
                }
                requiredEquipment.add(equipment);
            }
        }
        List<Room> rooms = roomRepository.findAll();
        Room bestRoom = null;
        long bestScore = Long.MAX_VALUE;
        for (Room room : rooms) {
            // Room must be available
            if (room.getStatus() != RoomStatus.AVAILABLE) {
                continue;
            }
            // Room must have enough capacity
            if (room.getCapacity() < request.getNumberOfParticipants()) {
                continue;
            }
            // Room must contain all required equipment
            boolean hasAllEquipment = true;
            for (String code : request.getRequiredEquipmentCodes() == null ? new HashSet<String>() : request.getRequiredEquipmentCodes()) {
                boolean found = false;
                for (Equipment equipment : room.getEquipment()) {
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
            // Room must not have a conflicting reservation
            boolean conflict = reservationRepository.existsByRoomAndStatusAndStartLessThanAndEndGreaterThan(
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
            if (bestRoom == null || score < bestScore) {
                bestRoom = room;
                bestScore = score;
            } else if (score == bestScore) {
                int nameComparison = room.getName().compareToIgnoreCase(bestRoom.getName());
                if (nameComparison < 0) {
                    bestRoom = room;
                } else if (nameComparison == 0 && room.getId() < bestRoom.getId()) {
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
        reservation.setNumberOfParticipants(request.getNumberOfParticipants()
        );
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setRequiredEquipment(requiredEquipment);
        return reservationRepository.save(reservation);
    }


}