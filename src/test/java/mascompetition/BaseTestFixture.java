package mascompetition;


import mascompetition.Entity.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;

/**
 * Tests extend this fixture which provides default models
 */
@ExtendWith({SpringExtension.class, MockitoExtension.class})
public class BaseTestFixture {

    public static String HASHED_ADMIN_PASSWORD = "$2a$10$7hZKRE1GM3dLBw23sIl4qOqb6Ze8OijNGqzHFBs3kfdGQKHOJ4AeC";

    /**
     * Creates a default user for testing
     *
     * @return The builder to allow modifications before creation
     */
    public User.UserBuilder getUser() {

        return User.builder()
                .authorities(new HashSet<>())
                .email("default@email.com")
                .id(UUID.randomUUID())
                .hashedPassword("Password1!");
    }

    /**
     * Creates a default team for testing
     *
     * @return The builder to allow modifications before creation
     */
    public Team.TeamBuilder getTeam() {

        return Team.builder()
                .id(UUID.randomUUID())
                .name("Default team name")
                .users(new ArrayList<>());
    }

    public Agent.AgentBuilder getAgent() {
        return Agent.builder()
                .id(UUID.randomUUID())
                .name("DefaultAgent")
                .versionNumber(1)
                .team(mock(Team.class))
                .glickoRating(GlickoRating.newRating())
                .status(AgentStatus.UNVALIDATED)
                .authors(List.of(mock(User.class)));
    }

}
