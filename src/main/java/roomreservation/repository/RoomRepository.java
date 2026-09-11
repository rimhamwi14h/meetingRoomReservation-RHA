package roomreservation.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import roomreservation.model.Room;
public interface RoomRepository extends JpaRepository<Room, Long> {
}
