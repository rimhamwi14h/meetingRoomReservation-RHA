package roomreservation.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import roomreservation.model.Equipment;
import roomreservation.repository.EquipmentRepository;

import java.util.Comparator;
import java.util.List;

@Service
public class EquipmentService {
    @Autowired
    private EquipmentRepository equipmentRepository;
    public Equipment createEquipment(Equipment equipment){
        return equipmentRepository.save(equipment);
    }
    public List<Equipment> getAllEquipment(){
        List<Equipment> equipment = equipmentRepository.findAll();
        equipment.sort(
                Comparator.comparing(Equipment::getCode)
        );
        return equipment;
    }
}
