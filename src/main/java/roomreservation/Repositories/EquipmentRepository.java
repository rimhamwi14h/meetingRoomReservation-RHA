package roomreservation.Repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import roomreservation.model.Equipment;
public interface EquipmentRepository extends JpaRepository<Equipment, Long>{
}
