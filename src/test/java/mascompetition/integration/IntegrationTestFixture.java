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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

/**
 * Integration tests for application
 * <p>
 * The database is completely mocked to avoid the use of an embedded database with a different syntax from MariaDB
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "default@email.com")
public class IntegrationTestFixture extends BaseTestFixture {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected UserRepository userRepository;

    @MockBean
    protected TeamRepository teamRepository;

    @MockBean
    protected PasswordEncoder passwordEncoder;

    protected ObjectMapper mapper = new ObjectMapper();

    protected User currentUser;

    @BeforeEach
    void resetMocks() {
        currentUser = getUser().build();
        lenient().when(userRepository.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
        lenient().when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        lenient().when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        lenient().when(teamRepository.save(any())).thenAnswer(invocation -> invocation.<Team>getArgument(0));

        lenient().when(passwordEncoder.matches(any(), any())).thenReturn(true);
        lenient().when(passwordEncoder.encode(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }
}
