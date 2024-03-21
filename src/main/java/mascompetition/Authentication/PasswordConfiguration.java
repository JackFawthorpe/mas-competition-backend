package mascompetition.Authentication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration for the encryption of passwords
 */
@Configuration
public class PasswordConfiguration {

    /**
     * Specifies BCrypt as the password encoder to use
     * @return The password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {return new BCryptPasswordEncoder();}
}
