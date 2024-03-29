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
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:80", "http://localhost:443")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
