package roomreservation.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import roomreservation.model.Building;
public interface BuildingRepository extends JpaRepository<Building, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
