package roomreservation.model;
import jakarta.persistence.*;
import lombok.Data;
@Entity
@Table(name= "buildings")
@Data
public class Building {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(name = "number_of_floors")
    private Integer numberOfFloors;
}
