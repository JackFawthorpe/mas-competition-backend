package mascompetition.unit.BLL;

import mascompetition.BLL.AgentService;
import mascompetition.BLL.GameScheduler;
import mascompetition.BLL.GameService;
import mascompetition.BaseTestFixture;
import mascompetition.Email.EmailService;
import mascompetition.Entity.Agent;
import mascompetition.Entity.GlickoRating;
import mascompetition.Exception.EngineFailureException;
import mascompetition.Exception.LoadAgentException;
import mascompetition.Repository.AgentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @Mock
    private EmailService emailService;

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
    void runGames_Bluesky() throws EngineFailureException, IOException, LoadAgentException, InterruptedException {
        List<Agent> agents = new ArrayList();

        for (int i = 0; i < 4; i++) {
            agents.add(getAgent().build());
        }

        when(gameService.runGame(any())).thenReturn(List.of(1, 1, 1, 1));
        when(agentService.getAllAgents()).thenReturn(agents);

        gameScheduler.runGames();

        verify(agentRepository, times(1)).saveAll(any());
    }

    @Test
    void runGames_EngineFailure_NoAgentSaving() throws EngineFailureException, IOException, LoadAgentException, InterruptedException {
        List<Agent> agents = new ArrayList();

        for (int i = 0; i < 4; i++) {
            agents.add(getAgent().build());
        }

        when(gameService.runGame(any())).thenThrow(new LoadAgentException("Failed to load agent"));
        when(agentService.getAllAgents()).thenReturn(agents);

        gameScheduler.runGames();

        verify(agentRepository, times(0)).saveAll(agents);
    }

    @Test
    void runGames_IntegrationTest_OnlySavesPlayedAgents() throws EngineFailureException, IOException, LoadAgentException, InterruptedException {
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

    @Test
    void verifyRatings_ExtremeRatingChange_SendsEmail() {
        GlickoRating mockedGlickoRating = mock(GlickoRating.class);
        when(mockedGlickoRating.getRating()).thenReturn(1000.0);
        when(mockedGlickoRating.getNextRating()).thenReturn(3001.0);
        when(mockedGlickoRating.getId()).thenReturn(UUID.randomUUID());
        gameScheduler.verifyRatings(List.of(mockedGlickoRating), List.of(1));

        verify(emailService, times(1)).sendSimpleMessage(eq("fawthorp878@gmail.com"), eq("MAS-COMPETITION CRITICAL RATING ERROR"), anyString());
    }

    @Test
    void verifyRatings_GoodRatingChange_NoEmail() {
        GlickoRating mockedGlickoRating = mock(GlickoRating.class);
        when(mockedGlickoRating.getRating()).thenReturn(1000.0);
        when(mockedGlickoRating.getNextRating()).thenReturn(2999.0);
        gameScheduler.verifyRatings(List.of(mockedGlickoRating), List.of(1));

        verify(emailService, times(0)).sendSimpleMessage(any(), any(), any());
    }
}
