package mascompetition.unit.BLL;

import mascompetition.BLL.TeamService;
import mascompetition.BaseTestFixture;
import mascompetition.DTO.CreateTeamDTO;
import mascompetition.DTO.TeamDTO;
import mascompetition.Entity.Team;
import mascompetition.Repository.TeamRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

class TeamServiceTest extends BaseTestFixture {


    @InjectMocks
    TeamService teamService;

    @Mock
    TeamRepository teamRepository;


    @BeforeEach
    void resetMocks() {
        when(teamRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }


    @Test
    void createUsers_NonemptyList_SavesAllUsers() {

        CreateTeamDTO createTeamDTO = new CreateTeamDTO("Default Team Name");

        TeamDTO returned = teamService.createTeam(createTeamDTO);

        ArgumentCaptor<Team> argumentCaptor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository, times(1)).save(argumentCaptor.capture());
        Team savedTeam = argumentCaptor.getValue();
        Assertions.assertEquals(createTeamDTO.getName(), savedTeam.getName());
        Assertions.assertNotNull(savedTeam.getId());

        Assertions.assertEquals(0, returned.getUsers().size());
        Assertions.assertEquals(savedTeam.getName(), returned.getName());
        Assertions.assertEquals(savedTeam.getId(), returned.getId());
    }
}
