package mascompetition.BLL;

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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@Service
public class GameScheduler {

    Logger logger = LoggerFactory.getLogger(GameScheduler.class);

    @Autowired
    private GameService gameService;

    @Autowired
    private AgentRepository agentRepository;

    @Value("${next-round-cron-expression}")
    private String nextRoundCronExpression;

    @Scheduled(cron = "${next-round-cron-expression}")
    @Transactional
    public void myTask() {
        logger.info("Scheduled Event Started: Agent Evaluation");
        Iterable<Agent> agentIterable = agentRepository.findAll();
        List<Agent> agents = StreamSupport.stream(agentIterable.spliterator(), false).toList();
        logger.info("Loaded {} agents into the game", agents.size());
        Map<Integer, List<Agent>> matchGroups = IntStream.range(0, (agents.size() + 3) / 4)
                .boxed()
                .collect(Collectors.toMap(
                        i -> i,
                        i -> agents.subList(i * 4, Math.min((i + 1) * 4, agents.size()))
                ));

        for (int i = 0; i < matchGroups.size(); i++) {
            List<Agent> agentGroup = matchGroups.get(i);
            if (agentGroup.size() != 4) {
                return;
            }
            List<Integer> points = gameService.runGame(agentGroup);

            if (points.size() != 4) {
                return;
            }

            handleRatingsUpdate(agentGroup, points);
            agentRepository.saveAll(agentGroup);

            logger.info("Scores: {} {} {} {}", points.get(0), points.get(1), points.get(2), points.get(3));
        }
    }


    @Transactional
    public void handleRatingsUpdate(List<Agent> agents, List<Integer> points) {

        logger.info("Starting Ratings calculations");
        List<GlickoRating> ratings = agents.stream().map(Agent::getGlickoRating).toList();

        for (int i = 0; i < 4; i++) {
            List<Double> scores = new ArrayList<>();
            List<GlickoRating> opponents = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                if (j == i) continue;
                if (points.get(i) > points.get(j)) {
                    scores.add(1.0);
                } else if (points.get(i) == points.get(j)) {
                    scores.add(0.5);
                } else {
                    scores.add(0.0);
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
