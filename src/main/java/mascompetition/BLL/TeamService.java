package mascompetition.BLL;

import jakarta.validation.constraints.NotNull;
import mascompetition.DTO.CreateTeamDTO;
import mascompetition.DTO.TeamDTO;
import mascompetition.Entity.Team;
import mascompetition.Entity.User;
import mascompetition.Repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
     */
    public TeamDTO createTeam(@NotNull CreateTeamDTO createTeamDTO) {
        Team team = teamRepository.save(Team.builder()
                .id(UUID.randomUUID())
                .name(createTeamDTO.getName())
                .build());
        logger.info("Created new team {}", team.getId());
        return TeamDTO.builder()
                .id(team.getId())
                .users(team.getUsers() == null
                        ? new ArrayList<>()
                        : team.getUsers().stream().map(User::getId).toList())
                .name(team.getName()).build();
    }

}
