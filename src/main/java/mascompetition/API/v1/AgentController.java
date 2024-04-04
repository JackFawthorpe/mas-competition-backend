package mascompetition.API.v1;

import jakarta.validation.Valid;
import mascompetition.API.BaseController;
import mascompetition.BLL.AgentService;
import mascompetition.BLL.GameScheduler;
import mascompetition.BLL.LeaderboardService;
import mascompetition.BLL.UserService;
import mascompetition.DTO.AgentListDTO;
import mascompetition.DTO.CreateAgentDTO;
import mascompetition.DTO.CreateAgentResponseDTO;
import mascompetition.Exception.ActionForbiddenException;
import mascompetition.Exception.AgentStorageException;
import mascompetition.Exception.BadInformationException;
import mascompetition.Exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Controller responsible for interaction with agents
 */
@RestController
@RequestMapping("/api/v1")
public class AgentController extends BaseController {

    Logger logger = LoggerFactory.getLogger(AgentController.class);

    @Autowired
    private AgentService agentService;

    @Autowired
    private GameScheduler gameScheduler;

    @Autowired
    private UserService userService;

    @Autowired
    private LeaderboardService leaderboardService;

    /**
     * Endpoint for creating a new agent
     *
     * @param agentSourceCode The source code provided with the agent
     * @param createAgentDTO  The metadata of the agent
     * @param result          The simple validation performed on the metadata of the agent
     * @return 201 with the agent ID and next time for evaluation given it is successful
     * @throws BadInformationException  Thrown if invalid metadata is provided for the agent
     * @throws ActionForbiddenException Thrown if trying to include an author that is not in the team
     * @throws AgentStorageException    Thrown if there is an issue persisting the agent
     * @throws EntityNotFoundException  Thrown if one of the members of the team doesn't exist
     */
    @PostMapping(value = "/agents")
    public ResponseEntity<CreateAgentResponseDTO> createAgent(
            @RequestPart("source") MultipartFile agentSourceCode,
            @RequestPart("data") @Valid CreateAgentDTO createAgentDTO,
            BindingResult result) throws BadInformationException, ActionForbiddenException, AgentStorageException, EntityNotFoundException {
        logger.info("POST api/v1/agents with user {}", userService.getCurrentUser().getId());

        validateEndpoint(result);

        UUID createdAgentID = agentService.createAgent(createAgentDTO, agentSourceCode);

        return ResponseEntity.status(HttpStatus.CREATED).body(CreateAgentResponseDTO.builder()
                .agentID(createdAgentID)
                .nextRound(gameScheduler.getNextRound())
                .build());
    }


    /**
     * Endpoint for retrieving the agents in a leaderboard format of rating descending
     *
     * @return The
     */
    @GetMapping(value = "/agents")
    public ResponseEntity<List<AgentListDTO>> getAgents() {
        logger.info("GET api/vi/agents with user {}", userService.getCurrentUser().getId());

        return ResponseEntity.status(HttpStatus.OK).body(leaderboardService.getOrderedAgentLeaderboard());
    }
}
