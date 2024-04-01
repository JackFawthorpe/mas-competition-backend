package mascompetition.BLL;

import mascompetition.Entity.User;
import mascompetition.Exception.ActionForbiddenException;
import mascompetition.Exception.BadInformationException;
import mascompetition.Exception.EntityNotFoundException;
import mascompetition.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;

@Component
public class AgentValidator {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

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
    public List<User> validateAuthors(List<String> emails) throws EntityNotFoundException, BadInformationException, ActionForbiddenException {
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
