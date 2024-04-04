package mascompetition.Repository;

import mascompetition.Entity.Agent;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * CRUD repository for accessing agents
 */
@Repository
public interface AgentRepository extends CrudRepository<Agent, UUID> {

    @Query(value = "SELECT a.* FROM agent a JOIN glicko_rating gr ON a.glicko_rating_id = gr.id ORDER BY gr.rating DESC", nativeQuery = true)
    List<Agent> findAllByOrderByRatingDesc();
}
