package mascompetition.integration;

import mascompetition.Repository.UserRepository;
import mascompetition.UnitTestFixture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for application
 * <p>
 * The database is completely mocked to avoid the use of an embedded database with a different syntax from MariaDB
 */
@SpringBootTest
@AutoConfigureMockMvc
public class IntegrationTestFixture extends UnitTestFixture {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected UserRepository userRepository;
}
