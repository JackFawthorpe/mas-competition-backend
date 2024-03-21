package mascompetition.Repository;

import mascompetition.Entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * CRUD repository for accessing user data
 */
@Repository
public interface UserRepository extends CrudRepository<User, UUID> {

    User findByEmail(String email);

}
