package mascompetition.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import mascompetition.BaseTestFixture;
import mascompetition.Entity.Team;
import mascompetition.Entity.User;
import mascompetition.Repository.TeamRepository;
import mascompetition.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

/**
 * Integration tests for application
 * <p>
 * The database is completely mocked to avoid the use of an embedded database with a different syntax from MariaDB
 */
@SpringBootTest
@AutoConfigureMockMvc
public class IntegrationTestFixture extends BaseTestFixture {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected UserRepository userRepository;

    @MockBean
    protected TeamRepository teamRepository;

    protected ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void resetMocks() {
        lenient().when(userRepository.findByEmail("user")).thenReturn(getUser().build());
        lenient().when(userRepository.save(any())).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        lenient().when(teamRepository.save(any())).thenAnswer(invocation -> {
            Team team = invocation.getArgument(0);
            team.setId(UUID.randomUUID());
            return team;
        });
    }
}
