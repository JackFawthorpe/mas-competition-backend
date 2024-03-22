package mascompetition.Authentication;


import jakarta.validation.constraints.NotNull;
import mascompetition.Entity.User;
import mascompetition.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService implements AuthenticationProvider {
    Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserRepository userRepository;

    /**
     * @param authentication the authentication request object.
     * @return Given a valid authentication it returns a token to identify the user
     */
    @Override
    public Authentication authenticate(@NotNull Authentication authentication) throws BadCredentialsException {
        String email = String.valueOf(authentication.getName());
        String password = String.valueOf(authentication.getCredentials());

        if (authentication.getName() == null || email.isEmpty() || authentication.getCredentials() == null || password.isEmpty()) {
            logger.info("Authentication failed because: missing email || password");
            throw new BadCredentialsException("Bad Credentials");
        }

        User user = userRepository.findByEmail(email);

        if (user == null) {
            logger.info("Authentication failed because: Email {} is not in use", email);
            throw new BadCredentialsException("Invalid email");
        }

        if (!passwordEncoder.matches(password, user.getHashedPassword())) {
            logger.info("Authentication failed because: Incorrect password provided for user {}", user.getId());
            throw new BadCredentialsException("Invalid Password");
        }

        logger.info("Authentication Succeeded for user: {}", user.getId());
        return new UsernamePasswordAuthenticationToken(user.getEmail(), null, user.getAuthorities());
    }

    /**
     * Returns true if the provided authentication provided is a UsernamePasswordAuthenticationToken
     *
     * @param authentication
     * @return true if the implementation can more closely evaluate the
     * Authentication class presented
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
