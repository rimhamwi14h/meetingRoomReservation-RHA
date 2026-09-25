package roomreservation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import roomreservation.exception.ResourceAlreadyExistsException;
import roomreservation.model.Equipment;
import roomreservation.repository.EquipmentRepository;
import roomreservation.request.CreateEquipmentRequest;

import java.util.Comparator;
import java.util.List;


/**
 * Service responsible for equipment management.
 *
 * It handles equipment creation, uniqueness validation
 * and retrieval of all available equipment.
 */
@Service
public class EquipmentService {

    @Autowired
    private EquipmentRepository equipmentRepository;


    /**
     * Creates a new equipment.
     *
     * The equipment code must be unique.
     *
     * @param request equipment creation information
     * @return the created equipment
     */
    public Equipment createEquipment(
            CreateEquipmentRequest request) {

        Equipment existingEquipment =
                equipmentRepository.findByCode(
                        request.getCode()
                );

        if (existingEquipment != null) {

            throw new ResourceAlreadyExistsException(
                    "An equipment already uses this code"
            );
        }

        Equipment equipment =
                new Equipment();

        equipment.setCode(
                request.getCode()
        );

        equipment.setLabel(
                request.getLabel()
        );

        return equipmentRepository.save(
                equipment
        );
    }


    /**
     * Returns all equipment sorted by code.
     *
     * @return sorted list of equipment
     */
    public List<Equipment> getAllEquipment() {

        List<Equipment> equipment =
                equipmentRepository.findAll();

        equipment.sort(
                Comparator.comparing(
                        Equipment::getCode
                )
        );

        return equipment;
    }
}