package mascompetition.unit.BLL;

import mascompetition.BLL.UserService;
import mascompetition.BaseTestFixture;
import mascompetition.DTO.UserDTO;
import mascompetition.Entity.User;
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
        lenient().when(userRepository.save(any())).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        lenient().when(passwordEncoder.encode(any())).thenAnswer(invocation -> invocation.getArgument(0));
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
        when(userRepository.findByEmail(any())).thenReturn(user);

        Assertions.assertEquals(user, userService.getCurrentUser());
    }

    @Test
    void createUsers_EmptyList_EmptyList() {
        Assertions.assertEquals(List.of(), userService.createUsers(List.of()));
    }

    @Test
    void createUsers_NonemptyList_SavesAllUsers() {
        List<UserDTO> users = List.of(new UserDTO("email1", "password"), new UserDTO("email2", "password"));

        userService.createUsers(users);
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
