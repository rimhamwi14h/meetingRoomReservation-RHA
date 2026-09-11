package roomreservation.Repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import roomreservation.model.Reservation;
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
