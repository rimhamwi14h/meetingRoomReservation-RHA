package roomreservation.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import roomreservation.model.AvailableRoomResponse;
import roomreservation.model.Room;

import roomreservation.request.CreateRoomRequest;
import roomreservation.request.ReplaceRoomEquipmentRequest;
import roomreservation.request.UpdateRoomRequest;
import roomreservation.request.UpdateRoomStatusRequest;

import roomreservation.service.RoomService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import java.net.URI;
@RestController
@RequestMapping("/api/rooms")
@Validated
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping
    public ResponseEntity<Room> createRoom(
            @Valid @RequestBody CreateRoomRequest request) {

        Room room = roomService.createRoom(request);

        URI location = URI.create(
                "/api/rooms/" + room.getId()
        );

        return ResponseEntity
                .created(location)
                .body(room);
    }

    @GetMapping
    public List<Room> getAllRooms() {

        return roomService.getAllRooms();
    }


    @GetMapping("/{id}")
    public Room getRoom(
            @PathVariable Long id) {

        return roomService.getRoom(id);
    }


    @PutMapping("/{id}")
    public Room updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoomRequest request) {

        return roomService.updateRoom(id, request);
    }


    @PatchMapping("/{id}/status")
    public Room updateRoomStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoomStatusRequest request) {

        return roomService.updateRoomStatus(id, request);
    }


    @PutMapping("/{id}/equipment")
    public Room replaceRoomEquipment(
            @PathVariable Long id,
            @Valid @RequestBody ReplaceRoomEquipmentRequest request) {

        return roomService.replaceRoomEquipment(id, request);
    }


    @GetMapping("/available")
    public List<AvailableRoomResponse> findAvailableRooms(
            @RequestParam OffsetDateTime start,
            @RequestParam OffsetDateTime end,
            @RequestParam @Min(1) Integer capacity,
            @RequestParam(required = false)
            Set<
                    @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,49}$")
                            String> equipment) {

        return roomService.findAvailableRooms(
                start,
                end,
                capacity,
                equipment
        );
    }
}