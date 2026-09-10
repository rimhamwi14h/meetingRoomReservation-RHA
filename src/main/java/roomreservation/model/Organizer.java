package roomreservation.model;
import jakarta.persistence.*;
import lombok.Data;
@Entity
@Table(name = "organizers")
@Data
public class Organizer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    @ManyToOne
    @JoinColumn(name = "Building_id")
    private Building building;
    private Integer floor;
}
