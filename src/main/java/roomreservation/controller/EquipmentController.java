package roomreservation.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import roomreservation.model.Equipment;
import roomreservation.service.EquipmentService;
import java.util.List;
@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {
    @Autowired
    private EquipmentService equipmentService;
    @PostMapping
    public Equipment createEquipment(@RequestBody Equipment equipment){
        return equipmentService.createEquipment(equipment);
    }
    @GetMapping
    public List<Equipment> getAllEquipment(){
        return equipmentService.getAllEquipment();
    }
}
