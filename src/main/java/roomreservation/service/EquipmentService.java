package roomreservation.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import roomreservation.model.Equipment;
import roomreservation.repository.EquipmentRepository;

import java.util.Comparator;
import java.util.List;
import roomreservation.request.CreateEquipmentRequest;
import roomreservation.exception.ResourceAlreadyExistsException;
@Service
public class EquipmentService {
    @Autowired
    private EquipmentRepository equipmentRepository;
    public Equipment createEquipment(CreateEquipmentRequest request) {

        Equipment existingEquipment =
                equipmentRepository.findByCode(request.getCode());

        if (existingEquipment != null) {
            throw new ResourceAlreadyExistsException(
                    "An equipment already uses this code"
            );
        }

        Equipment equipment = new Equipment();

        equipment.setCode(request.getCode());
        equipment.setLabel(request.getLabel());

        return equipmentRepository.save(equipment);
    }    public List<Equipment> getAllEquipment(){
        List<Equipment> equipment = equipmentRepository.findAll();
        equipment.sort(
                Comparator.comparing(Equipment::getCode)
        );
        return equipment;
    }
}
