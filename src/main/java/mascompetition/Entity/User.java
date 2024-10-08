package mascompetition.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import mascompetition.DTO.UserDTO;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

/**
 * JPA entity for the representation of a user
 */
@Builder
@Entity
@Data
@AllArgsConstructor
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(unique = true)
    private String email;

    @Column
    private String hashedPassword;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_authority",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "authority_id")
    )
    private Set<Authority> authorities;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Agent> agents;


    public User() {
        // JPA Empty Args Constructor
    }

    @Override
    public String toString() {
        return String.format("{email=%s, id=%s}", email, id.toString());
    }

    public UserDTO buildDTO() {
        return UserDTO.builder()
                .id(this.id)
                .email(this.email)
                .teamId(this.team == null ? null : this.team.getId())
                .build();
    }
}
