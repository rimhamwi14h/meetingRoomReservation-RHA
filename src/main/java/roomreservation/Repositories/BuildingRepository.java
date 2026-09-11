package roomreservation.Repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import roomreservation.model.Building;
public interface BuildingRepository extends JpaRepository<Building, Long> {
}
