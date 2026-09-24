package roomreservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomreservation.model.Room;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByBuildingId(Long buildingId);
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}