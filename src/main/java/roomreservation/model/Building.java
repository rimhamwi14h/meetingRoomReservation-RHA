package roomreservation.model;
import jakarta.persistence.*;
@Entity
@Table(name= "buildings")
public class Building {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(name = "number_of_floors")
    private Integer numberOfFloors;
}
