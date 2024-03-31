package mascompetition.Authentication;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for CORS headers
 */
@Configuration
public class CORSConfiguration implements WebMvcConfigurer {

    /**
     * Adds the frontend server to the accepted list of origins
     *
     * @param registry The registry containing CORS information
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                .addMapping("/**")
                .allowedOrigins("http://localhost:3000",                 // Development
                        "https://mas-competition.csse.canterbury.ac.nz", // Front-end assets are served from here
                        "http://132.181.18.82"                           // Backdoor to be removed once DNS is setup
                )
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
