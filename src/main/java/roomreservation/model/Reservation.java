package roomreservation.model;
import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.Set;
@Entity
@Table(name = "reservations")
@Data
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private Organizer organizer;
    @Column(name = "start_time")
    private OffsetDateTime start;
    @Column(name = "end_time")
    private OffsetDateTime end;
    @Column(name = "number_of_participants")
    private Integer numberOfParticipants;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.CONFIRMED;
    @Column(name = "created_at")
    private OffsetDateTime createdAt=OffsetDateTime.now();
    @ManyToMany
    @JoinTable(
            name = "reservation_required_equipment",
            joinColumns = @JoinColumn(name = "reservation_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id")
    )
    private Set<Equipment> requiredEquipment;
}
