package mascompetition.Repository;

import mascompetition.Entity.Team;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * CRUD repository for accessing team data
 */
@Repository
public interface TeamRepository extends CrudRepository<Team, UUID> {

}
