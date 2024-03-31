package mascompetition.integration.API.v1;

import mascompetition.DTO.ChangePasswordDTO;
import mascompetition.integration.IntegrationTestFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserAPITest extends IntegrationTestFixture {
    @Test
    void changePassword_Bluesky_201() throws Exception {

        ChangePasswordDTO changepasswordDTO = ChangePasswordDTO.builder()
                .currentPassword("Password1!")
                .newPassword("Password2!")
                .confirmPassword("Password2!")
                .build();

        String toSend = mapper.writeValueAsString(changepasswordDTO);

        mockMvc.perform(post("/api/v1/users/" + currentUser.getId() + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toSend))
                .andExpect(status().isOk());

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void changePassword_IncorrectPassword_400() throws Exception {

        when(passwordEncoder.matches(currentUser.getHashedPassword(), "Password1!")).thenReturn(false);

        ChangePasswordDTO changepasswordDTO = ChangePasswordDTO.builder()
                .currentPassword("Password1!")
                .newPassword("Password2!")
                .confirmPassword("Password2!")
                .build();

        String toSend = mapper.writeValueAsString(changepasswordDTO);

        mockMvc.perform(post("/api/v1/users/" + currentUser.getId() + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toSend))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid current password")));
    }

    @Test
    void changePassword_NonMatchingNewAndConfirm_400() throws Exception {

        ChangePasswordDTO changepasswordDTO = ChangePasswordDTO.builder()
                .currentPassword("Password1!")
                .newPassword("Password2!")
                .confirmPassword("Password3!")
                .build();

        String toSend = mapper.writeValueAsString(changepasswordDTO);

        mockMvc.perform(post("/api/v1/users/" + currentUser.getId() + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toSend))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Confirmation Password doesn't match New Password")));
    }


    @ParameterizedTest
    @ValueSource(strings = {"", "A1!", "NoNumber", "nocapital1", "TooLarge1!TooLarge1!T"})
    void changePassword_BadNewPassword_400(String newPassword) throws Exception {

        ChangePasswordDTO changepasswordDTO = ChangePasswordDTO.builder()
                .currentPassword("Password1!")
                .newPassword(newPassword)
                .confirmPassword(newPassword)
                .build();

        String toSend = mapper.writeValueAsString(changepasswordDTO);

        mockMvc.perform(post("/api/v1/users/" + currentUser.getId() + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toSend))
                .andExpect(status().isBadRequest());
    }


    @Test
    void changePassword_missingPassword_400() throws Exception {

        ChangePasswordDTO changepasswordDTO = ChangePasswordDTO.builder()
                .newPassword("Password1!")
                .confirmPassword("Password1!")
                .build();

        String toSend = mapper.writeValueAsString(changepasswordDTO);

        mockMvc.perform(post("/api/v1/users/" + currentUser.getId() + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toSend))
                .andExpect(status().isBadRequest());
    }


    @Test
    void changePassword_missingConfirmation_400() throws Exception {

        ChangePasswordDTO changepasswordDTO = ChangePasswordDTO.builder()
                .currentPassword("Password1!")
                .newPassword("Password1!")
                .build();

        String toSend = mapper.writeValueAsString(changepasswordDTO);

        mockMvc.perform(post("/api/v1/users/" + currentUser.getId() + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toSend))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_missingNewPassword_400() throws Exception {

        ChangePasswordDTO changepasswordDTO = ChangePasswordDTO.builder()
                .currentPassword("Password1!")
                .confirmPassword("Password1!")
                .build();

        String toSend = mapper.writeValueAsString(changepasswordDTO);

        mockMvc.perform(post("/api/v1/users/" + currentUser.getId() + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toSend))
                .andExpect(status().isBadRequest());
    }


    @Test
    void changePassword_DifferentUser_403() throws Exception {

        ChangePasswordDTO changepasswordDTO = ChangePasswordDTO.builder()
                .currentPassword("Password1!")
                .newPassword("Password2!")
                .confirmPassword("Password2!")
                .build();

        String toSend = mapper.writeValueAsString(changepasswordDTO);

        mockMvc.perform(post("/api/v1/users/" + UUID.randomUUID() + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toSend))
                .andExpect(status().isForbidden());
    }

}
