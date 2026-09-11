package roomreservation.Repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import roomreservation.model.Organizer;
public interface OrganizerRepository extends JpaRepository<Organizer, Long>{
}
