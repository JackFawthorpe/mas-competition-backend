package mascompetition.unit.BLL;

import mascompetition.BLL.AgentService;
import mascompetition.BLL.LeaderboardService;
import mascompetition.BaseTestFixture;
import mascompetition.DTO.AgentListDTO;
import mascompetition.Entity.Agent;
import mascompetition.Entity.GlickoRating;
import mascompetition.Entity.Team;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.mockito.Mockito.when;

class LeaderboardServiceTest extends BaseTestFixture {

    @InjectMocks
    private LeaderboardService leaderboardService;

    @Mock
    private AgentService agentService;


    @Test
    void getOrderedAgentLeaderboard() {
        Team team = getTeam().build();
        GlickoRating glickoRating = GlickoRating.newRating();
        Agent agent = getAgent().team(team).glickoRating(glickoRating).build();

        when(agentService.getAllAgents()).thenReturn(List.of(agent));

        List<AgentListDTO> result = leaderboardService.getOrderedAgentLeaderboard();

        AgentListDTO expected = AgentListDTO.builder()
                .agentName(String.format("%s v%s", agent.getName(), agent.getVersionNumber()))
                .teamName(team.getName())
                .agentId(agent.getId())
                .teamId(team.getId())
                .rating(1500)
                .build();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(expected, result.get(0));
    }

}
