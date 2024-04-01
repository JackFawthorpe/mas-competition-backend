package mascompetition.unit.BLL;

import mascompetition.BLL.AgentValidator;
import mascompetition.BLL.UserService;
import mascompetition.BaseTestFixture;
import mascompetition.Entity.Team;
import mascompetition.Entity.User;
import mascompetition.Exception.ActionForbiddenException;
import mascompetition.Exception.BadInformationException;
import mascompetition.Exception.EntityNotFoundException;
import mascompetition.Repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

public class AgentValidatorTest extends BaseTestFixture {

    @InjectMocks
    AgentValidator agentValidator;

    @Mock
    UserService userService;

    @Mock
    UserRepository userRepository;

    User currentUser;

    User inTeam;

    User outOfTeam;

    @BeforeEach
    void resetMocks() {
        Team team = getTeam().build();
        currentUser = getUser().email("1").team(team).build();
        inTeam = getUser().email("2").team(team).build();
        outOfTeam = getUser().email("3").team(getTeam().build()).build();

        lenient().when(userService.getCurrentUser()).thenReturn(currentUser);
        lenient().when(userRepository.findByEmail("1")).thenReturn(Optional.of(currentUser));
        lenient().when(userRepository.findByEmail("2")).thenReturn(Optional.of(inTeam));
        lenient().when(userRepository.findByEmail("3")).thenReturn(Optional.of(outOfTeam));
    }

    @Test
    void validateAuthors_blueskyScenario() throws BadInformationException, ActionForbiddenException, EntityNotFoundException {
        Assertions.assertEquals(List.of(inTeam, currentUser), agentValidator.validateAuthors(List.of("2", "1")));
    }


    @Test
    void validateAuthors_authorNotInTeam_ThrowsActionForbidden() {

        currentUser.setTeam(null);

        Assertions.assertThrows(ActionForbiddenException.class, () -> agentValidator.validateAuthors(List.of("2", "1")));
    }


    @Test
    void validateAuthors_differentTeams_ThrowsActionForbidden() {
        Assertions.assertThrows(ActionForbiddenException.class, () -> agentValidator.validateAuthors(List.of("3", "1")));
    }


    @Test
    void validateAuthors_authorNotInAuthorList_ThrowsBadInformation() {
        Assertions.assertThrows(BadInformationException.class, () -> agentValidator.validateAuthors(List.of("2", "3")));
    }

    @Test
    void validateAuthors_userDoesntExist_EntityNotFoundException() {
        when(userRepository.findByEmail("2")).thenReturn(Optional.empty());
        Assertions.assertThrows(EntityNotFoundException.class, () -> agentValidator.validateAuthors(List.of("2", "3")));
    }

}
