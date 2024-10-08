package mascompetition.BLL;

import jakarta.validation.constraints.NotNull;
import mascompetition.Email.EmailService;
import mascompetition.Entity.Agent;
import mascompetition.Entity.AgentStatus;
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

    private final double MAX_ACCEPTABLE_RATING_CHANGE = 2000.0f;
    Logger logger = LoggerFactory.getLogger(GameScheduler.class);
    @Autowired
    private GameService gameService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private EmailService emailService;

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
            List<Integer> agentPoints;
            try {
                agentPoints = gameService.runGame(agentGroup);
            } catch (Exception e) {
                logger.warn("Game failed to run with the following agents {} {} {} {} : With exception {}",
                        agentGroup.get(0).getId(),
                        agentGroup.get(1).getId(),
                        agentGroup.get(2).getId(),
                        agentGroup.get(3).getId(),
                        e.getMessage());
                continue;
            }

            List<GlickoRating> ratings = agentGroup.stream().map(Agent::getGlickoRating).toList();
            handleRatingsUpdate(ratings, agentPoints);
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
        List<Agent> agents = new ArrayList<>(agentService.getAllAgents().stream()
                .filter(agent -> agent.getStatus() == AgentStatus.AVAILABLE || agent.getStatus() == AgentStatus.UNVALIDATED)
                .toList());
        Collections.shuffle(agents);
        List<List<Agent>> matches = new ArrayList<>();
        for (int i = 0; i + 3 < agents.size(); i += 4) {
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
     * @param ratings The ratings of each agents within teh game
     * @param points  The amount of points they scored in the game
     */
    @Transactional
    public void handleRatingsUpdate(@NotNull List<GlickoRating> ratings, @NotNull List<Integer> points) {
        logger.info("Starting Ratings calculations");
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
        verifyRatings(ratings, points);
        ratings.forEach(GlickoRating::updateRating);
    }

    /**
     * This is a clamping function to avoid climb to infinity rating scenarios.
     * At the time of writing this, although ratings initially function as expected overtime 1-2 agents will have their ratings grow exponentially
     * This method is responsible for firing a warning email to increase visibility of when the issue happens as well as provide inputs to identify why it may be happening
     * <p>
     * It will also clamp the ratings growth to avoid visible behaviour problems
     */
    public void verifyRatings(List<GlickoRating> glickoRatings, List<Integer> points) {
        boolean cancelledRatingChange = false;
        for (GlickoRating rating : glickoRatings) {
            if (Math.abs(rating.getRating() - rating.getNextRating()) > MAX_ACCEPTABLE_RATING_CHANGE) {
                cancelledRatingChange = true;
            }
        }

        if (cancelledRatingChange) {
            logger.warn("Detected Abnormal rating change with agents {}", glickoRatings);
            emailService.sendSimpleMessage(
                    "fawthorp878@gmail.com",
                    "MAS-COMPETITION CRITICAL RATING ERROR",
                    "Rating change significantly higher than predicted. Printing State Dump.\n\nAgent ID's:\n"
                            + glickoRatings.stream().map(GlickoRating::getId).toList()
                            + "\n\nCurrent Ratings\n\n"
                            + glickoRatings.stream().map(GlickoRating::getRating).toList()
                            + "\n\nNext Ratings\n\n"
                            + glickoRatings.stream().map(GlickoRating::getNextRating).toList()
                            + "\n\nPoints:\n\n"
                            + points.toString()
            );
            for (GlickoRating rating : glickoRatings) {
                rating.cancelRatingChange();
            }
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
