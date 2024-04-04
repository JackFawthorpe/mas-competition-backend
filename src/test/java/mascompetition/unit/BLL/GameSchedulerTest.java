package mascompetition.unit.BLL;

import mascompetition.BLL.AgentService;
import mascompetition.BLL.GameScheduler;
import mascompetition.BLL.GameService;
import mascompetition.BaseTestFixture;
import mascompetition.Entity.Agent;
import mascompetition.Entity.GlickoRating;
import mascompetition.Repository.AgentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.fail;

class GameSchedulerTest extends BaseTestFixture {

    @InjectMocks
    GameScheduler gameScheduler;

    @Mock
    private AgentService agentService;

    @Mock
    private GameService gameService;

    @Mock
    private AgentRepository agentRepository;

    @Test
    void handleRatingsUpdate_bluesky() {
        List<GlickoRating> ratings = List.of(
                spy(GlickoRating.newRating()),
                spy(GlickoRating.newRating()),
                spy(GlickoRating.newRating()),
                spy(GlickoRating.newRating())
        );

        List<Integer> points = List.of(1, 2, 2, 3);

        gameScheduler.handleRatingsUpdate(ratings, points);

        for (GlickoRating rating : ratings) {
            verify(rating, times(1)).updateRating();
        }

        verify(ratings.get(0), times(1))
                .calculateNewRating(ratings.stream().filter(rating -> !rating.equals(ratings.get(0))).toList(),
                        List.of(0.0, 0.0, 0.0));

        verify(ratings.get(1), times(1))
                .calculateNewRating(ratings.stream().filter(rating -> !rating.equals(ratings.get(1))).toList(),
                        List.of(1.0, 0.5, 0.0));

        verify(ratings.get(2), times(1))
                .calculateNewRating(ratings.stream().filter(rating -> !rating.equals(ratings.get(2))).toList(),
                        List.of(1.0, 0.5, 0.0));

        verify(ratings.get(3), times(1))
                .calculateNewRating(ratings.stream().filter(rating -> !rating.equals(ratings.get(3))).toList(),
                        List.of(1.0, 1.0, 1.0));
    }


    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void getGameGroups_NoTeams(int agentCount) {
        List<Agent> agents = new ArrayList();

        for (int i = 0; i < agentCount; i++) {
            agents.add(getAgent().build());
        }
        when(agentService.getAllAgents()).thenReturn(agents);

        Assertions.assertEquals(0, gameScheduler.getGameGroups().size());
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 6, 7})
    void getGameGroups_OneMatch(int agentCount) {
        List<Agent> agents = new ArrayList();

        for (int i = 0; i < agentCount; i++) {
            agents.add(getAgent().build());
        }
        when(agentService.getAllAgents()).thenReturn(agents);

        List<List<Agent>> result = gameScheduler.getGameGroups();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(4, result.get(0).size());
    }

    @Test
    void getGameGroups_TwoMatches() {
        List<Agent> agents = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            agents.add(getAgent().build());
        }
        when(agentService.getAllAgents()).thenReturn(agents);

        List<List<Agent>> result = gameScheduler.getGameGroups();

        Assertions.assertEquals(2, result.size());
        for (Agent agent : agents) {
            if (!result.get(0).contains(agent) && !result.get(1).contains(agent)) {
                fail("Agent missing");
            }
        }
    }

    @Test
    void runGames_Bluesky() {
        List<Agent> agents = new ArrayList();

        for (int i = 0; i < 4; i++) {
            agents.add(getAgent().build());
        }

        when(gameService.runGame(any())).thenReturn(List.of(1, 1, 1, 1));
        when(agentService.getAllAgents()).thenReturn(agents);

        gameScheduler.runGames();

        verify(agentRepository, times(1)).saveAll(agents);
    }

    @Test
    void runGames_IntegrationTest_OnlySavesPlayedAgents() {
        List<Agent> agents = new ArrayList();

        for (int i = 0; i < 5; i++) { // One of these wont be saved
            agents.add(getAgent().build());
        }

        when(gameService.runGame(any())).thenReturn(List.of(1, 1, 1, 1));
        when(agentService.getAllAgents()).thenReturn(agents);

        gameScheduler.runGames();

        ArgumentCaptor<List<Agent>> argumentCaptor = ArgumentCaptor.forClass(List.class);

        verify(agentRepository, times(1)).saveAll(argumentCaptor.capture());
        Assertions.assertEquals(4, argumentCaptor.getValue().size());
    }
}
