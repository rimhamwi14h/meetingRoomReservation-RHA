package roomreservation.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import roomreservation.model.*;
import roomreservation.repository.BuildingRepository;
import roomreservation.repository.EquipmentRepository;
import roomreservation.repository.RoomRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Service
public class RoomService {
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private BuildingRepository buildingRepository;
    @Autowired
    private EquipmentRepository equipmentRepository;
    public Room createRoom(CreateRoomRequest request){
        Building building = buildingRepository.findById(request.getBuildingId()).orElse(null);
        if (building==null){
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
}
