package roomreservation.model;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;
@Entity
@Table(name = "rooms")
@Data
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @ManyToOne
    @JoinColumn(name = "building_id")
    private Building building;
    private Integer floor;
    private Integer capacity;
    @Enumerated(EnumType.STRING)
    private RoomStatus status = RoomStatus.AVAILABLE;
    @ManyToMany
    @JoinTable(
            name = "room_equipment",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id")
    )
    private Set<Equipment> equipment;
}