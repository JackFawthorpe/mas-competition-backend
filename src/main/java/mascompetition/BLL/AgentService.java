package mascompetition.BLL;

import mascompetition.DTO.CreateAgentDTO;
import mascompetition.Entity.Agent;
import mascompetition.Entity.Team;
import mascompetition.Entity.User;
import mascompetition.Exception.ActionForbiddenException;
import mascompetition.Exception.AgentStorageException;
import mascompetition.Exception.BadInformationException;
import mascompetition.Exception.EntityNotFoundException;
import mascompetition.Repository.AgentRepository;
import mascompetition.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class AgentService {

    Logger logger = LoggerFactory.getLogger(AgentService.class);

    @Value("${agentStoragePath}")
    private String agentDir;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private UserService userService;

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

        List<User> authors = validateAuthors(createAgentDTO.getEmails());

        UUID agentID = UUID.randomUUID();
        logger.info("Saving agent with ID: {}", agentID);

        Team team = userService.getCurrentUser().getTeam();

        Path agentPath = Path.of(agentDir + team.getId() + '/' + createAgentDTO.getName() + '_' + createAgentDTO.getVersionNumber() + ".java");

        try {
            Files.createDirectories(agentPath.getParent());
            Files.copy(file.getInputStream(), agentPath, StandardCopyOption.REPLACE_EXISTING);
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
                .build();

        try {
            agentRepository.save(agent);
        } catch (Exception e1) {
            try {
                Files.delete(agentPath); // Definitely exists
            } catch (IOException e2) {
                throw new AgentStorageException(e2.getMessage());
            }
            throw new AgentStorageException(e1.getMessage());
        }

        return agentID;
    }

    /**
     * Checks if the provided authors can make an agent together against the following rules:
     * <p>
     * - The current user must be in the author list
     * - The current user must be in a team
     * - All other authors are in the same team
     *
     * @param emails The list of authors emails
     * @throws EntityNotFoundException  Thrown if one of the authors doesn't exist
     * @throws ActionForbiddenException Thrown if one of the authors isn't on the current users team
     * @throws BadInformationException  Thrown if the author isn't on the author list
     */
    private List<User> validateAuthors(List<String> emails) throws EntityNotFoundException, BadInformationException, ActionForbiddenException {
        try {
            List<User> users = emails.stream()
                    .map(email -> userRepository.findByEmail(email).orElseThrow())
                    .toList();

            User currentUser = userService.getCurrentUser();

            boolean notInAuthorsList = !users.contains(currentUser);
            boolean notInTeam = currentUser.getTeam() == null;
            boolean authorsInDifferentTeams = users.stream().map(User::getTeam).distinct().count() != 1;

            if (notInAuthorsList) {
                throw new BadInformationException("You must be in the author list");
            }

            if (notInTeam || authorsInDifferentTeams) {
                throw new ActionForbiddenException("All authors must be in your team");
            }

            return users;
        } catch (NoSuchElementException ex) {
            throw new EntityNotFoundException("Email doesn't exist");
        }
    }
}
