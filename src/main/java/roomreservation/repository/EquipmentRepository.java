package roomreservation.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import roomreservation.model.Equipment;
public interface EquipmentRepository extends JpaRepository<Equipment, Long>{
    Equipment findByCode(String code);

}
