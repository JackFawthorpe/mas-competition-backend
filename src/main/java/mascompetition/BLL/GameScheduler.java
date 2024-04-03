package mascompetition.BLL;

import mascompetition.Entity.Agent;
import mascompetition.Repository.AgentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@Component
public class GameScheduler {

    Logger logger = LoggerFactory.getLogger(GameScheduler.class);

    @Autowired
    private GameService gameService;

    @Autowired
    private AgentRepository agentRepository;

    @Scheduled(fixedRate = 60000)
    public void myTask() {
        logger.info("Starting game evaluation");
        Iterable<Agent> agentIterable = agentRepository.findAll();
        List<Agent> agents = StreamSupport.stream(agentIterable.spliterator(), false).toList();

        Map<Integer, List<Agent>> matchGroups = IntStream.range(0, (agents.size() + 3) / 4)
                .boxed()
                .collect(Collectors.toMap(
                        i -> i,
                        i -> agents.subList(i * 4, Math.min((i + 1) * 4, agents.size()))
                ));

        matchGroups.forEach((key, value) -> {
            if (value.size() != 4) {
                return;
            }
            List<Integer> scores = gameService.runGame(value);

            if (scores.size() == 4) {
                logger.info("Scores: {} {} {} {}", scores.get(0), scores.get(1), scores.get(2), scores.get(3));
            }
        });
    }
}
