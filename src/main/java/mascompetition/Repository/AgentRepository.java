package mascompetition.Repository;

import mascompetition.Entity.Agent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * CRUD repository for accessing agents
 */
@Repository
public interface AgentRepository extends CrudRepository<Agent, UUID> {

}
