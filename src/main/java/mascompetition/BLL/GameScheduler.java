package mascompetition.BLL;

import jakarta.validation.constraints.NotNull;
import mascompetition.Entity.Agent;
import mascompetition.Entity.GlickoRating;
import mascompetition.Repository.AgentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service responsible for handling the scheduling and orchestrating the execution of games from the {@link GameService}
 */
@Service
public class GameScheduler {

    Logger logger = LoggerFactory.getLogger(GameScheduler.class);

    @Autowired
    private GameService gameService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentRepository agentRepository;

    @Value("${next-round-cron-expression}")
    private String nextRoundCronExpression;

    /**
     * The head of the game scheduling system
     * <p>
     * It will first fetch all the agents and organise them into groups
     * It will then run each game and on the assumption the game ran successfully it will update the rankings
     * of each of the agents
     */
    @Scheduled(cron = "${next-round-cron-expression}")
    @Transactional
    public void runGames() {
        logger.info("Scheduled Event Started: Agent Evaluation");

        for (List<Agent> agentGroup : getGameGroups()) {
            List<Integer> agentPoints = gameService.runGame(agentGroup);

            // If the game didn't run successfully
            if (agentPoints.size() != 4) {
                logger.error("Game failed to run with agents {} {} {} {}",
                        agentGroup.get(0).getId(),
                        agentGroup.get(1).getId(),
                        agentGroup.get(2).getId(),
                        agentGroup.get(3).getId());
                continue;
            }

            handleRatingsUpdate(agentGroup, agentPoints);
            agentRepository.saveAll(agentGroup);

            logger.info("Scores for game: {} {} {} {}",
                    agentPoints.get(0),
                    agentPoints.get(1),
                    agentPoints.get(2),
                    agentPoints.get(3));
        }
    }

    /**
     * Generates the groupings of agents to be played
     *
     * @return The list of agents grouped in matches
     */
    public List<List<Agent>> getGameGroups() {
        List<Agent> agents = agentService.getAllAgents();
        Collections.shuffle(agents);
        List<List<Agent>> matches = new ArrayList<>();
        for (int i = 0; i + 3 <= agents.size(); i += 4) {
            matches.add(agents.subList(i, i + 4));
        }
        return matches;
    }

    /**
     * Logic for updating the ratings of agents after they have played a match
     * Rating updates are as follows:
     * - A Single game of 4 players is treated as 3 one on ones
     * - Your rating is calculated based on the expectation of you getting more points than each of your opponents
     * - You are then evaluated based on those games and your rating is updated
     *
     * @param agents The list of agents within a game
     * @param points The amount of points they scored in the game
     */
    @Transactional
    public void handleRatingsUpdate(@NotNull List<Agent> agents, @NotNull List<Integer> points) {
        logger.info("Starting Ratings calculations");

        List<GlickoRating> ratings = agents.stream().map(Agent::getGlickoRating).toList();

        for (int i = 0; i < ratings.size(); i++) {

            List<Double> scores = new ArrayList<>();
            List<GlickoRating> opponents = new ArrayList<>();

            for (int j = 0; j < ratings.size(); j++) {
                if (j == i) continue;
                if (points.get(i) > points.get(j)) {
                    scores.add(1.0);
                } else if (points.get(i) < points.get(j)) {
                    scores.add(0.0);
                } else {
                    scores.add(0.5);
                }
                opponents.add(ratings.get(j));
            }
            ratings.get(i).calculateNewRating(opponents, scores);
        }
        ratings.forEach(GlickoRating::updateRating);
        for (int i = 0; i < 4; i++) {
            logger.info("Storing new rating of {} for agent {}", ratings.get(i).getRating(), agents.get(i).getId());
        }
    }

    /**
     * Finds the next time a round will start
     *
     * @return The next time a round will start
     */
    public ZonedDateTime getNextRound() {
        CronExpression cronExpression = CronExpression.parse(nextRoundCronExpression);
        return cronExpression.next(ZonedDateTime.now());
    }

}
