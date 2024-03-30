package mascompetition.integration.API.v1;

import mascompetition.DTO.UserLoginDTO;
import mascompetition.integration.IntegrationTestFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(roles = {"ADMIN"})
class AdminAPITest extends IntegrationTestFixture {
    @Test
    void addUsers_OneUser_201() throws Exception {

        List<UserLoginDTO> input = new ArrayList<>();
        input.add(new UserLoginDTO("first@email.com", "Password1!"));

        String toSend = mapper.writeValueAsString(input);

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toSend))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "A1!", "NoNumber", "nocapital1", "TooLarge1!TooLarge1!T"})
    void addUsers_badPassword_400(String badPassword) throws Exception {

        List<UserLoginDTO> input = new ArrayList<>();
        input.add(new UserLoginDTO("first@email.com", badPassword));

        String toSend = mapper.writeValueAsString(input);

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toSend))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUsers_NoPassword_401() throws Exception {

        List<UserLoginDTO> input = new ArrayList<>();
        input.add(new UserLoginDTO("email@email.com", null));

        String toSend = mapper.writeValueAsString(input);

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toSend))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUsers_NoEmail_401() throws Exception {

        List<UserLoginDTO> input = new ArrayList<>();
        input.add(new UserLoginDTO(null, "ValidPassword1!"));

        String toSend = mapper.writeValueAsString(input);

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toSend))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"user@example", "user@example.", "user.example.com", "userexample.com",
            "user@.com", "user@com", "user@", "user"})
    void addUsers_InvalidEmail_401() throws Exception {

        List<UserLoginDTO> input = new ArrayList<>();
        input.add(new UserLoginDTO(null, "ValidPassword1!"));

        String toSend = mapper.writeValueAsString(input);

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toSend))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUsers_emailInUser_401() throws Exception {

        doThrow(new DataIntegrityViolationException("Email in use")).when(userRepository).save(any());

        List<UserLoginDTO> input = new ArrayList<>();
        input.add(new UserLoginDTO("first@email.com", "Password1!"));

        String toSend = mapper.writeValueAsString(input);

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toSend))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid Data Provided: Duplicate email detected"));
    }

    @Test
    @WithMockUser
    void addUsers_notAnAdmin_403() throws Exception {

        List<UserLoginDTO> input = new ArrayList<>();
        input.add(new UserLoginDTO("first@email.com", "Password1!"));

        String toSend = mapper.writeValueAsString(input);

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toSend))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Default Team", "Team 1", "111111", "teamteamteamteamteam"})
    void createTeam_bluesky_201(String teamName) throws Exception {

        mockMvc.perform(post("/api/v1/admin/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"" + teamName + "\""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(teamName));

        verify(teamRepository, times(1)).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"12311", "teamteamteamteamteam1", "asdasd!", "team12\n", "team12;", "te<am12"})
    void createTeam_InvalidTeamName_400(String teamName) throws Exception {

        mockMvc.perform(post("/api/v1/admin/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"" + teamName + "\""))
                .andExpect(status().isBadRequest());

        verify(teamRepository, times(0)).save(any());
    }

    @Test
    @WithMockUser
    void createTeam_notAnAdmin_403() throws Exception {
        mockMvc.perform(post("/api/v1/admin/team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"Default Team\""))
                .andExpect(status().isForbidden());
    }


    @Test
    void createTeam_DuplicateTeamName_400() throws Exception {

        when(teamRepository.save(any())).thenThrow(new DataIntegrityViolationException("Not user facing"));

        mockMvc.perform(post("/api/v1/admin/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"Default Team\""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid Data Provided: Duplicate team name detected"));

        verify(teamRepository, times(1)).save(any());
    }


    @Test
    void createTeam_NoBody_400() throws Exception {

        mockMvc.perform(post("/api/v1/admin/teams"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Request does not match API requirements"));

        verify(teamRepository, times(0)).save(any());
    }
}
