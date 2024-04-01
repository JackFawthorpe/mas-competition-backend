package mascompetition.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Builder
@Entity
@Data
@AllArgsConstructor
public class Agent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column
    private int designTime;


    @ManyToMany(fetch = FetchType.LAZY)
    private List<User> authors;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;


    @Column
    private String name;

    @Column
    private int versionNumber;

    public Agent() {
        // JPA Empty Args Constructor
    }
}
