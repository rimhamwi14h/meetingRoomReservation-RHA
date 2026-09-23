package roomreservation.model;
import jakarta.persistence.*;
import lombok.Data;
@Entity
@Data
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String label;
}
