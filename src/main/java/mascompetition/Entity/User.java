package mascompetition.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity for the representation of a user
 */
@Builder()
@Entity
@Data
@AllArgsConstructor
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column
    private String email;
    @Column
    private String hashedPassword;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Authority> authorities;

    public User() {
        // JPA Empty Args Constructor
    }
}
