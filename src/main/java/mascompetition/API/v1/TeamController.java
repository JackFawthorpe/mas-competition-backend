package mascompetition.API.v1;

import mascompetition.API.BaseController;
import mascompetition.BLL.LeaderboardService;
import mascompetition.BLL.TeamService;
import mascompetition.BLL.UserService;
import mascompetition.DTO.TeamDTO;
import mascompetition.DTO.TeamLeaderboardDTO;
import mascompetition.Exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controller for team based endpoints
 */
@RestController
@RequestMapping("/api/v1")
public class TeamController extends BaseController {
    Logger logger = LoggerFactory.getLogger(TeamController.class);

    @Autowired
    TeamService teamService;

    @Autowired
    UserService userService;

    @Autowired
    LeaderboardService leaderboardService;

    /**
     * Endpoint for getting a singular team
     *
     * @param teamId The ID of the team
     * @return the TeamDTO of the team (200)
     * @throws EntityNotFoundException Thrown if the team is not found
     */
    @GetMapping("/teams/{team_id}")
    public ResponseEntity<TeamDTO> getTeam(
            @PathVariable(value = "team_id") UUID teamId
    ) throws EntityNotFoundException {
        logger.info("GET /api/v1/teams/{}", teamId);
        return ResponseEntity.status(HttpStatus.OK).body(teamService.getTeam(teamId));
    }


    /**
     * Endpoint for retrieving the agents in a leaderboard format of rating descending
     *
     * @return The list of teams as per {@link LeaderboardService#getTeamLeaderboard}
     */
    @GetMapping(value = "/teams")
    public ResponseEntity<List<TeamLeaderboardDTO>> getAgents() {
        logger.info("GET api/vi/agents with user {}", userService.getCurrentUser().getId());

        return ResponseEntity.status(HttpStatus.OK).body(leaderboardService.getTeamLeaderboard());
    }

}
