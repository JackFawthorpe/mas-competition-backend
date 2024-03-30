package mascompetition.Entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.UUID;

/**
 * Global Authorities for a user.
 * Many-to-many table that maps users to Springboot security roles
 * <p>
 * Roles:
 * - User: The default that is applied to all users that have registered
 * - Administrator: Role provided by developers to users with permissions to create competitions
 */
@Entity
public class Authority implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column()
    private String role;

    /**
     * Empty Constructor for JPA
     */
    protected Authority() {
        // JPA constructor
    }

    public Authority(String authority) {
        this.role = authority;
    }

    @Override
    public String getAuthority() {
        return role;
    }
}
