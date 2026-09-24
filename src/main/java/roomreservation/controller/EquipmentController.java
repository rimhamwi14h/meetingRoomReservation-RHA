package roomreservation.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import roomreservation.model.Equipment;
import roomreservation.request.CreateEquipmentRequest;
import roomreservation.service.EquipmentService;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @PostMapping
    public Equipment createEquipment(
            @Valid @RequestBody CreateEquipmentRequest request) {

        return equipmentService.createEquipment(request);
    }

    @GetMapping
    public List<Equipment> getAllEquipment() {

        return equipmentService.getAllEquipment();
    }
}