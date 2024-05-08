package mascompetition.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import mascompetition.DTO.TeamDTO;
import mascompetition.DTO.TeamLeaderboardDTO;

import java.util.List;
import java.util.UUID;

/**
 * JPA entity for the representation of a team
 * <p>
 * The teamLeaderboardDTOResult is used within {@link mascompetition.Repository.TeamRepository}
 * to fetch the highest rated teams
 */
@SqlResultSetMapping(name = "teamLeaderboardDTOResult",
        classes = {
                @ConstructorResult(targetClass = TeamLeaderboardDTO.class,
                        columns = {
                                @ColumnResult(name = "teamId"),
                                @ColumnResult(name = "teamName"),
                                @ColumnResult(name = "agentId"),
                                @ColumnResult(name = "agentRating"),
                                @ColumnResult(name = "agentName")
                        })
        })
@Builder
@Entity
@Data
@AllArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(unique = true)
    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "team")
    private List<User> users;

    public Team() {
        // JPA Empty Args Constructor
    }

    public TeamDTO buildDTO() {
        return TeamDTO.builder()
                .id(this.getId())
                .name(this.getName())
                .users(this.getUsers().stream().map(User::buildDTO).toList())
                .build();
    }

}
