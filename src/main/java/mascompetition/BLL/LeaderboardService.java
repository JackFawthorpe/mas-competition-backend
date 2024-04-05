package mascompetition.BLL;

import mascompetition.DTO.AgentListDTO;
import mascompetition.Entity.Agent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Functions for handling get agent requests
 */
@Service
public class LeaderboardService {

    @Autowired
    private AgentService agentService;

    /**
     * Retrieves all the agents within the database, orders them by rating and returns them
     *
     * @return The AgentListDTO version of the agent
     */
    public List<AgentListDTO> getOrderedAgentLeaderboard() {
        List<Agent> agents = agentService.getAllAgents();

        return agents.stream().map(agent ->
                AgentListDTO.builder()
                        .agentId(agent.getId())
                        .agentName(String.format("%s v%s", agent.getName(), agent.getVersionNumber()))
                        .teamId(agent.getTeam().getId())
                        .teamName(agent.getTeam().getName())
                        .rating((int) Math.floor(agent.getGlickoRating().getRating()))
                        .build()
        ).toList();
    }

}
