package roomreservation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    public Room createRoom(CreateRoomRequest request) {
        Building building = buildingRepository.findById(request.getBuildingId()).orElse(null);
        if (building == null) {
            return null;
        }
        Set<Equipment> equipment = new HashSet<>();
        if (request.getEquipmentCodes() != null) {
            for (String code : request.getEquipmentCodes()) {
                Equipment e = equipmentRepository.findByCode(code);
                if (e == null) {
                    return null;
                }
                equipment.add(e);
            }
        }
        Room room = new Room();
        room.setName(request.getName());
        room.setBuilding(building);
        room.setFloor(request.getFloor());
        room.setCapacity(request.getCapacity());
        room.setStatus(RoomStatus.AVAILABLE);
        room.setEquipment(equipment);
        return roomRepository.save(room);
    }
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }
    public Room getRoom(Long id) {
        return roomRepository.findById(id).orElse(null);
    }
    public Room updateRoom(Long id, UpdateRoomRequest request) {
        Room room = roomRepository.findById(id).orElse(null);
        if (room == null) {
            return null;
        }
        Building building = buildingRepository.findById(request.getBuildingId()).orElse(null);
        if (building == null) {
            return null;
        }
        room.setName(request.getName());
        room.setBuilding(building);
        room.setFloor(request.getFloor());
        room.setCapacity(request.getCapacity());
        return roomRepository.save(room);
    }
    public Room updateRoomStatus(Long id, UpdateRoomStatusRequest request) {
        Room room = roomRepository.findById(id).orElse(null);
        if (room == null) {
            return null;
        }
        room.setStatus(request.getStatus());
        return roomRepository.save(room);
    }
    public Room replaceRoomEquipment(
            Long id,
            ReplaceRoomEquipmentRequest request) {
        Room room = roomRepository.findById(id).orElse(null);
        if (room == null) {
            return null;
        }
        Set<Equipment> equipment = new HashSet<>();
        for (String code : request.getEquipmentCodes()) {
            Equipment e = equipmentRepository.findByCode(code);
            if (e == null) {
                return null;
            }
            equipment.add(e);
        }
        room.setEquipment(equipment);
        return roomRepository.save(room);
    }
    public List<AvailableRoomResponse> findAvailableRooms(
            OffsetDateTime start,
            OffsetDateTime end,
            Integer capacity,
            Set<String> equipmentCodes) {
        List<Room> rooms = roomRepository.findAll();
        List<AvailableRoomResponse> availableRooms = new ArrayList<>();
        for (Room room : rooms) {
            // Room must be AVAILABLE
            if (room.getStatus() != RoomStatus.AVAILABLE) {
                continue;
            }
            // Room must have enough capacity
            if (room.getCapacity() < capacity) {
                continue;
            }
            // Check required equipment
            boolean hasAllEquipment = true;
            if (equipmentCodes != null) {
                for (String code : equipmentCodes) {
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
            }
            if (!hasAllEquipment) {
                continue;
            }
            // Check reservation conflict
            boolean conflict =
                    reservationRepository.existsByRoomAndStatusAndStartLessThanAndEndGreaterThan(room, ReservationStatus.CONFIRMED, end, start);
            if (conflict) {
                continue;
            }
            // Create response
            AvailableRoomResponse response = new AvailableRoomResponse();
            response.setId(room.getId());
            response.setName(room.getName());
            response.setBuilding(room.getBuilding());
            response.setFloor(room.getFloor());
            response.setCapacity(room.getCapacity());
            response.setStatus(room.getStatus());
            response.setEquipment(room.getEquipment());
            response.setUnusedCapacity(room.getCapacity() - capacity);
            availableRooms.add(response);
        }
        // Sort after checking ALL rooms
        availableRooms.sort(Comparator.comparing(AvailableRoomResponse::getUnusedCapacity).thenComparing(AvailableRoomResponse::getName, String.CASE_INSENSITIVE_ORDER).thenComparing(AvailableRoomResponse::getId));
        return availableRooms;
    }
}