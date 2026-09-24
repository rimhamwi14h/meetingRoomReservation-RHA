package roomreservation.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import roomreservation.model.Equipment;
import roomreservation.request.CreateEquipmentRequest;
import roomreservation.service.EquipmentService;

import java.util.List;
import org.springframework.http.ResponseEntity;
import java.net.URI;
@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @PostMapping
    public ResponseEntity<Equipment> createEquipment(
            @Valid @RequestBody CreateEquipmentRequest request) {

        Equipment equipment = equipmentService.createEquipment(request);

        URI location = URI.create(
                "/api/equipment/" + equipment.getId()
        );

        return ResponseEntity
                .created(location)
                .body(equipment);
    }

    @GetMapping
    public List<Equipment> getAllEquipment() {

        return equipmentService.getAllEquipment();
    }
}