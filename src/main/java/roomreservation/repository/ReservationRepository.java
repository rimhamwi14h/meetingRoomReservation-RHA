package roomreservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import roomreservation.model.Reservation;
import roomreservation.model.ReservationStatus;
import roomreservation.model.Room;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    boolean existsByRoomAndStatusAndStartLessThanAndEndGreaterThan(
            Room room,
            ReservationStatus status,
            OffsetDateTime end,
            OffsetDateTime start
    );

    Optional<Reservation>
    findFirstByRoomAndStatusAndStartLessThanAndEndGreaterThan(
            Room room,
            ReservationStatus status,
            OffsetDateTime end,
            OffsetDateTime start
    );
}