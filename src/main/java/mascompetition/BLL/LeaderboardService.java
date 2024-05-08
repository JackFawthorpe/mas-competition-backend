package mascompetition.BLL;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import mascompetition.DTO.AgentListDTO;
import mascompetition.DTO.TeamLeaderboardDTO;
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

    @Autowired
    private EntityManager em;

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
                        .status(agent.getStatus().toString())
                        .build()
        ).toList();
    }

    /**
     * Runs a native SQL query to fetch the teams and their highest rated agent
     *
     * @return A list of each team that has submitted an agent that is still available
     */
    public List<TeamLeaderboardDTO> getTeamLeaderboard() {
        String sqlString = """
                    SELECT t.id AS teamId, t.name AS teamName, MAX(ar.rating) AS agentRating, ar.agent_id AS agentId, ar.name as agentName
                    FROM team t JOIN (SELECT a.id AS agent_id, gr.rating, a.team_id, a.name, a.version_number
                                     FROM agent a JOIN glicko_rating gr ON a.glicko_rating_id = gr.id
                                     WHERE a.status = "AVAILABLE") ar ON t.id = ar.team_id                 
                    GROUP BY t.id
                    ORDER BY MAX(ar.rating) DESC
                """;
        Query query = em.createNativeQuery(sqlString, "teamLeaderboardDTOResult");
        return query.getResultList();
    }
}
