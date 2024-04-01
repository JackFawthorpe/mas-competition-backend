package mascompetition.unit.BLL;

import mascompetition.BLL.UserService;
import mascompetition.BaseTestFixture;
import mascompetition.DTO.ChangePasswordDTO;
import mascompetition.DTO.UserLoginDTO;
import mascompetition.Entity.User;
import mascompetition.Exception.BadInformationException;
import mascompetition.Repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest extends BaseTestFixture {

    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void resetMocks() {
        lenient().when(userRepository.findById(any())).thenReturn(Optional.of(getUser().build()));
        lenient().when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        lenient().when(passwordEncoder.matches(any(), any())).thenReturn(true);
        lenient().when(passwordEncoder.encode(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }


    @Test
    void changePassword_bluesky_SavesPassword() {
        ChangePasswordDTO changepasswordDTO = ChangePasswordDTO.builder()
                .currentPassword("Password1!")
                .newPassword("Password2!")
                .confirmPassword("Password2!")
                .build();


        Assertions.assertDoesNotThrow(() -> userService.changePassword(changepasswordDTO, mock(UUID.class)));

        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(argumentCaptor.capture());
        Assertions.assertEquals(changepasswordDTO.getNewPassword(), argumentCaptor.getValue().getHashedPassword());
    }

    @Test
    void changePassword_newPasswordMatchesOld_BadCredentials() {
        ChangePasswordDTO changepasswordDTO = ChangePasswordDTO.builder()
                .currentPassword("Password1!")
                .newPassword("Password1!")
                .confirmPassword("Password1!")
                .build();

        Throwable error = Assertions.assertThrows(BadInformationException.class, () -> userService.changePassword(changepasswordDTO, mock(UUID.class)));

        Assertions.assertEquals("The new password cannot match the old one", error.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void changePassword_currentPasswordIsWrong_BadCredentials() {
        ChangePasswordDTO changepasswordDTO = ChangePasswordDTO.builder()
                .currentPassword("Password1!")
                .newPassword("Password2!")
                .confirmPassword("Password2!")
                .build();

        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        Throwable error = Assertions.assertThrows(BadInformationException.class, () -> userService.changePassword(changepasswordDTO, mock(UUID.class)));

        Assertions.assertEquals("Invalid current password", error.getMessage());

        verify(userRepository, times(0)).save(any());
    }

    @Test
    void changePassword_confirmationPassowrdDoesntMatch_BadCredentials() {
        ChangePasswordDTO changepasswordDTO = ChangePasswordDTO.builder()
                .currentPassword("Password1!")
                .newPassword("Password3!")
                .confirmPassword("Password2!")
                .build();


        Throwable error = Assertions.assertThrows(BadInformationException.class, () -> userService.changePassword(changepasswordDTO, mock(UUID.class)));

        Assertions.assertEquals("Confirmation Password doesn't match New Password", error.getMessage());

        verify(userRepository, times(0)).save(any());
    }


    @Test
    @WithAnonymousUser
    void getCurrentUser_AnonymousAuthentication_returnsNull() {
        Assertions.assertNull(userService.getCurrentUser());
    }

    @Test
    @WithMockUser
    void getCurrentUser_loggedIn_ReturnsUsers() {
        User user = getUser().build();
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        Assertions.assertEquals(user, userService.getCurrentUser());
    }

    @Test
    void createUsers_EmptyList_EmptyList() {
        Assertions.assertEquals(List.of(), userService.batchCreateUsers(List.of()));
    }

    @Test
    void createUsers_NonemptyList_SavesAllUsers() {
        List<UserLoginDTO> users = List.of(new UserLoginDTO("email1", "password"), new UserLoginDTO("email2", "password"));

        userService.batchCreateUsers(users);
        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(2)).save(argumentCaptor.capture());
        List<User> capturedUsers = argumentCaptor.getAllValues();
        Assertions.assertEquals(2, capturedUsers.size());
        Assertions.assertEquals("email1", capturedUsers.get(0).getEmail());
        Assertions.assertEquals("password", capturedUsers.get(0).getHashedPassword());
        Assertions.assertEquals("email2", capturedUsers.get(1).getEmail());
        Assertions.assertEquals("password", capturedUsers.get(1).getHashedPassword());
    }
}
