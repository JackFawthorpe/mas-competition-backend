package mascompetition.BLL;

import mascompetition.DTO.CreateAgentDTO;
import mascompetition.Entity.*;
import mascompetition.Exception.ActionForbiddenException;
import mascompetition.Exception.AgentStorageException;
import mascompetition.Exception.BadInformationException;
import mascompetition.Exception.EntityNotFoundException;
import mascompetition.Repository.AgentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for interaction with agents
 */
@Service
public class AgentService {

    Logger logger = LoggerFactory.getLogger(AgentService.class);

    @Autowired
    AgentValidator agentValidator;

    @Value("${agentStoragePath}")
    private String agentDir;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private DirectoryService directoryService;

    /**
     * Method to get the path of an agent
     *
     * @param agent The agent to fetch the path for
     * @return The path the agent is stored at
     */
    public Path getAgentPath(Agent agent) {
        return Path.of(agentDir + agent.getTeam().getId() + '/' + agent.getId() + '/' + agent.getName() + '_' + agent.getVersionNumber() + ".java");
    }

    /**
     * Persists an agent
     *
     * @param createAgentDTO The DTO to create the agent from
     * @param file           The source code of the agent
     * @return The UUID of the created agent
     * @throws AgentStorageException    Thrown if there is an issue in persisting the data
     * @throws BadInformationException  Thrown if the currentUser isn't an author
     * @throws ActionForbiddenException Thrown if the user is trying to add authors outside their team
     * @throws EntityNotFoundException  Thrown if one of the provided authors doesn't exist
     */
    public UUID createAgent(CreateAgentDTO createAgentDTO, MultipartFile file) throws AgentStorageException, BadInformationException, ActionForbiddenException, EntityNotFoundException {

        List<User> authors = agentValidator.validateAuthors(createAgentDTO.getEmails());

        UUID agentID = UUID.randomUUID();
        logger.info("Saving agent with ID: {}", agentID);

        Team team = userService.getCurrentUser().getTeam();

        Path agentPath = Path.of(agentDir + team.getId() + '/' + agentID + '/' + createAgentDTO.getName() + '_' + createAgentDTO.getVersionNumber() + ".java");

        try {
            directoryService.saveFile(file.getInputStream(), agentPath);
        } catch (IOException e) {
            throw new AgentStorageException(e.getMessage());
        }

        Agent agent = Agent.builder()
                .id(agentID)
                .designTime(createAgentDTO.getDesignTime())
                .authors(authors)
                .team(team)
                .name(createAgentDTO.getName())
                .versionNumber(createAgentDTO.getVersionNumber())
                .glickoRating(GlickoRating.newRating())
                .status(AgentStatus.AVAILABLE)
                .build();

        try {
            agentRepository.save(agent);
        } catch (Exception e1) {
            try {
                // This could only fail if permissions for accessing this file change between storing and deleting
                directoryService.deleteFile(agentPath);
            } catch (IOException e2) {
                throw new AgentStorageException(e2.getMessage());
            }
            throw new AgentStorageException(e1.getMessage());
        }

        return agentID;
    }


    /**
     * Fetches all the agents in the game and loads them all into memory
     *
     * @return The list of agents
     */
    public List<Agent> getAllAgents() {
        List<Agent> agents = agentRepository.findAllByOrderByRatingDesc();
        logger.info("Loaded {} agents into memory", agents.size());
        return agents;
    }

    /**
     * Updates the agent's status
     *
     * @param agent       The agent to update
     * @param agentStatus The new status of the agent
     */
    public void setAgentStatus(Agent agent, AgentStatus agentStatus) {
        agent.setStatus(agentStatus);
        agentRepository.save(agent);
    }
}
