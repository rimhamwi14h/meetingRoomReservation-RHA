package roomreservation.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import roomreservation.model.*;
import roomreservation.request.CreateRoomRequest;
import roomreservation.request.UpdateRoomRequest;
import roomreservation.request.UpdateRoomStatusRequest;
import roomreservation.service.RoomService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import roomreservation.request.ReplaceRoomEquipmentRequest;
@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    @Autowired
    private RoomService roomService;

    @PostMapping
    public Room createRoom(@RequestBody CreateRoomRequest request) {
        return roomService.createRoom(request);
    }

    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/{id}")
    public Room getRoom(@PathVariable Long id) {
        return roomService.getRoom(id);
    }
    @PutMapping("/{id}")
    public Room updateRoom(
            @PathVariable Long id,
            @RequestBody UpdateRoomRequest request ) {
        return roomService.updateRoom(id, request);
    }
    @PatchMapping("/{id}/status")
    public Room updateRoomStatus (
            @PathVariable Long id,
            @RequestBody UpdateRoomStatusRequest request ) {
        return roomService.updateRoomStatus(id, request);
    }
    @PutMapping("/{id}/equipment")
    public Room replaceRoomEquipment(
            @PathVariable Long id,
            @RequestBody ReplaceRoomEquipmentRequest request){
        return roomService.replaceRoomEquipment(id,request);
    }
    @GetMapping("/available")
    public List<AvailableRoomResponse> findAvailableRooms(
            @RequestParam OffsetDateTime start,
            @RequestParam OffsetDateTime end,
            @RequestParam Integer capacity,
            @RequestParam(required = false) Set<String> equipment) {

        return roomService.findAvailableRooms(
                start,
                end,
                capacity,
                equipment
        );
    }
}
