package mascompetition.BLL;

import jakarta.validation.constraints.NotNull;
import mascompetition.DTO.CreateTeamDTO;
import mascompetition.DTO.TeamDTO;
import mascompetition.Entity.Team;
import mascompetition.Exception.EntityNotFoundException;
import mascompetition.Repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;


/**
 * Service responsible for business logic related to teams
 */
@Service
public class TeamService {


    Logger logger = LoggerFactory.getLogger(TeamService.class);

    @Autowired
    private TeamRepository teamRepository;

    /**
     * Persists the team
     *
     * @param createTeamDTO A validated DTO to create a team from
     * @return A TeamDTO of the team that was persisted
     * @throws DataIntegrityViolationException Thrown if the name of the team is already in use
     */
    public TeamDTO createTeam(@NotNull CreateTeamDTO createTeamDTO) throws DataIntegrityViolationException {
        Team team = teamRepository.save(Team.builder()
                .id(UUID.randomUUID())
                .name(createTeamDTO.getName())
                .build());
        logger.info("Created new team {}", team.getId());
        return TeamDTO.builder()
                .id(team.getId())
                .users(new ArrayList<>())
                .name(team.getName()).build();
    }

    /**
     * Fetches a team from the database
     *
     * @param teamId The ID of the team to fetch
     * @return The TeamDTO for the team
     * @throws EntityNotFoundException
     */
    public TeamDTO getTeam(@NotNull UUID teamId) throws EntityNotFoundException {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new EntityNotFoundException(String.format("Failed to find team with ID %s", teamId)));
        return team.buildDTO();
    }

}
