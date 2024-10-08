package mascompetition.Authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Custom endpoint security configuration
 */
@Configuration
@EnableWebSecurity
@ComponentScan
public class EndpointSecurityConfiguration {

    private static final String LOGIN_ENDPOINT = "/api/v1/login";
    private static final String LOGOUT_ENDPOINT = "/api/v1/logout";

    private static final String ADMIN_API_MATCHER = "/api/v1/admin/**";

    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    MasAuthenticationResponseHandler masAuthenticationResponseHandler;
    @Autowired
    MasAuthenticationLogoutResponseHandler masAuthenticationLogoutResponseHandler;

    /**
     * Creates an Authentication manager that is responsible for authenticating http requests based on the logged in user
     *
     * @param http The Http Security Configuration
     * @return The authentication Manager {@link AuthenticationService}
     * @throws Exception External exception
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(authenticationService);
        return authenticationManagerBuilder.build();
    }

    /**
     * Specifies the required roles and security configuration for end points in a tightening scope
     *
     * @param http The configuration for filtering
     * @return The build security filter chain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers(LOGIN_ENDPOINT).permitAll()
                                .requestMatchers(ADMIN_API_MATCHER).hasRole("ADMIN")
                                .anyRequest().authenticated()
                ).formLogin(formLogin ->
                        formLogin
                                .loginProcessingUrl(LOGIN_ENDPOINT)
                                .successHandler(masAuthenticationResponseHandler)
                                .failureHandler(masAuthenticationResponseHandler)
                                .usernameParameter("email")
                                .passwordParameter("password")
                                .permitAll()
                ).logout(logout ->
                        logout
                                .logoutUrl(LOGOUT_ENDPOINT)
                                .deleteCookies("JSESSIONID")
                                .logoutSuccessHandler(masAuthenticationLogoutResponseHandler)
                                .invalidateHttpSession(true)
                                .logoutSuccessUrl(LOGIN_ENDPOINT))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(new MasFailedAuthenticationResponse()));

        return http.build();
    }
}
