package roomreservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomreservation.model.Organizer;

import java.util.List;

public interface OrganizerRepository extends JpaRepository<Organizer, Long> {

    List<Organizer> findByBuildingId(Long buildingId);

    boolean existsByEmail(String email);
}