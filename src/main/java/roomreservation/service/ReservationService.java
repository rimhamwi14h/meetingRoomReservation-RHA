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
        Room room = roomRepository.findById(request.getRoomId()).orElse(null);
        if (room == null) {
            return null;
        }
        Organizer organizer = organizerRepository.findById(request.getOrganizerId()).orElse(null);
        if (organizer == null) {
            return null;
        }
        if (request.getStart() == null || request.getEnd() == null) {
            return null;
        }
        if (!request.getStart().isBefore(request.getEnd())) {
            return null;
        }
        if (request.getStart().isBefore(OffsetDateTime.now())) {
            return null;
        }
        if (Duration.between(request.getStart(), request.getEnd()).compareTo(Duration.ofHours(8)) > 0) {
            return null;
        }
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            return null;
        }
        if (room.getCapacity() < request.getNumberOfParticipants()) {
            return null;
        }
        Set<Equipment> requiredEquipment = new HashSet<>();
        if (request.getRequiredEquipmentCodes() != null) {
            for (String code : request.getRequiredEquipmentCodes()) {
                Equipment equipment = equipmentRepository.findByCode(code);
                if (equipment == null) {
                    return null;
                }
                if (!room.getEquipment().contains(equipment)) {
                    return null;
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
            return null;
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
        Reservation reservation = reservationRepository.findById(id).orElse(null);
        if (reservation == null) {
            return null;
        }
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return null;
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(reservation);
    }
    public Reservation getReservation (Long id) {
        return reservationRepository.findById(id).orElse(null);
    }
    public List<Reservation> getReservations (
            Long roomId,
            Long organizerId,
            OffsetDateTime from,
            OffsetDateTime to) {
        List<Reservation> reservations = reservationRepository.findAll();
        List<Reservation> result = new ArrayList<>();
        for(Reservation reservation : reservations){
            if(organizerId != null && !reservation.getRoom().getId().equals(roomId)){
                continue;
            }
            if (organizerId != null && !reservation.getOrganizer().getId().equals(organizerId)) {
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

}