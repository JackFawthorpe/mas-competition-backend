package mascompetition.unit.Authentication;

import mascompetition.Authentication.AuthenticationService;
import mascompetition.BaseTestFixture;
import mascompetition.Repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest extends BaseTestFixture {

    @InjectMocks
    AuthenticationService authenticationService;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    UserRepository userRepository;
    Authentication input;

    @BeforeEach
    void resetMocks() {
        lenient().when(passwordEncoder.matches(matches("password"), matches("Encoded Password"))).thenReturn(true);

        input = mock(Authentication.class);
        lenient().when(input.getName()).thenReturn("default@email.com");
        lenient().when(input.getCredentials()).thenReturn("password");

        lenient().when(userRepository.findByEmail("default@email.com")).thenReturn(getUser().build());
    }

    @Test
    void authenticate_bluesky_authenticatesSuccessfully() {

        Assertions.assertDoesNotThrow(() -> {
            authenticationService.authenticate(input);
        });

        verify(passwordEncoder, times(1))
                .matches(matches("password"), matches("Encoded Password"));

        verify(userRepository, times(1))
                .findByEmail(matches("default@email.com"));
    }

    @Test
    void authenticate_noEmailProvided_BadCredentials() {

        when(input.getName()).thenReturn(null);

        Throwable error = Assertions.assertThrows(BadCredentialsException.class, () -> {
            authenticationService.authenticate(input);
        });

        Assertions.assertEquals("Bad Credentials", error.getMessage());

        verify(passwordEncoder, times(0))
                .matches(matches("password"), matches("Encoded Password"));

        verify(userRepository, times(0))
                .findByEmail(matches("default@email.com"));
    }


    @Test
    void authenticate_EmailEmpty_BadCredentials() {

        when(input.getName()).thenReturn("");

        Throwable error = Assertions.assertThrows(BadCredentialsException.class, () -> {
            authenticationService.authenticate(input);
        });

        Assertions.assertEquals("Bad Credentials", error.getMessage());

        verify(passwordEncoder, times(0))
                .matches(matches("password"), matches("Encoded Password"));

        verify(userRepository, times(0))
                .findByEmail(matches("default@email.com"));
    }

    @Test
    void authenticate_nullPassword_BadCredentials() {

        when(input.getCredentials()).thenReturn(null);

        Throwable error = Assertions.assertThrows(BadCredentialsException.class, () -> {
            authenticationService.authenticate(input);
        });

        Assertions.assertEquals("Bad Credentials", error.getMessage());

        verify(passwordEncoder, times(0))
                .matches(matches("password"), matches("Encoded Password"));

        verify(userRepository, times(0))
                .findByEmail(matches("default@email.com"));
    }

    @Test
    void authenticate_PasswordEmpty_BadCredentials() {

        when(input.getCredentials()).thenReturn("");

        Throwable error = Assertions.assertThrows(BadCredentialsException.class, () -> {
            authenticationService.authenticate(input);
        });

        Assertions.assertEquals("Bad Credentials", error.getMessage());

        verify(passwordEncoder, times(0))
                .matches(matches("password"), matches("Encoded Password"));

        verify(userRepository, times(0))
                .findByEmail(matches("default@email.com"));
    }

    @Test
    void authenticate_userNotFound_BadCredentials() {

        when(userRepository.findByEmail(anyString())).thenReturn(null);

        Throwable error = Assertions.assertThrows(BadCredentialsException.class, () -> {
            authenticationService.authenticate(input);
        });

        Assertions.assertEquals("Invalid email", error.getMessage());

        verify(passwordEncoder, times(0))
                .matches(matches("password"), matches("Encoded Password"));

        verify(userRepository, times(1))
                .findByEmail(matches("default@email.com"));
    }

    @Test
    void authenticate_incorrectPassword_BadCredentials() {

        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        Throwable error = Assertions.assertThrows(BadCredentialsException.class, () -> {
            authenticationService.authenticate(input);
        });

        Assertions.assertEquals("Invalid Password", error.getMessage());

        verify(passwordEncoder, times(1))
                .matches(matches("password"), matches("Encoded Password"));

        verify(userRepository, times(1))
                .findByEmail(matches("default@email.com"));
    }
}
