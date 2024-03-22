package mascompetition.BLL;

import mascompetition.DTO.UserDTO;
import mascompetition.Entity.User;
import mascompetition.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Context that can be inherited by all controllers to provide information about the request
 */
@Service
public class UserService {

    Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Gets the user that has made the request
     *
     * @return The user logged in or null if there is no user
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication instanceof AnonymousAuthenticationToken
                ? null
                : userRepository.findByEmail(authentication.getName());
    }

    /**
     * Creates a transaction to create the users that are given to persist
     *
     * @param users The list of users to create
     * @return The list of UUID's of the users created
     * @throws DataIntegrityViolationException thrown when database transaction is unsuccessful. This will primarily be duplicate email
     */
    @Transactional
    public List<UUID> createUsers(List<UserDTO> users) throws DataIntegrityViolationException {
        logger.info("Attempting to persist users");
        List<UUID> ids = new ArrayList<>();
        for (UserDTO user : users) {
            ids.add(createUser(user));
        }
        return ids;
    }

    /**
     * Attempts to persist the User that is provided
     *
     * @param toCreate A validated DTO to persist
     * @return The UUID of the user created
     */
    public UUID createUser(UserDTO toCreate) {
        User createdUser = userRepository.save(
                User.builder()
                        .email(toCreate.getEmail())
                        .hashedPassword(passwordEncoder.encode(toCreate.getPassword()))
                        .build()
        );
        logger.info("Created new user {}", createdUser.getId());
        return createdUser.getId();
    }

}
