package mascompetition.integration.API.v1;

import mascompetition.DTO.UserLoginDTO;
import mascompetition.DTO.UserToTeamDTO;
import mascompetition.integration.IntegrationTestFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(roles = {"ADMIN"}, username = "default@email.com")
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


    @Test
    void addUsersToTeam_NoBody_400() throws Exception {

        mockMvc.perform(post("/api/v1/admin/teams/users"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Request does not match API requirements"));

        verify(userRepository, times(0)).save(any());
    }


    @Test
    @WithMockUser
    void addUsersToTeam_notAnAdmin_403() throws Exception {
        List<UserToTeamDTO> input = new ArrayList<>();
        input.add(new UserToTeamDTO("first@email.com", UUID.randomUUID()));

        String toSend = mapper.writeValueAsString(input);

        mockMvc.perform(post("/api/v1/admin/teams/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toSend))
                .andExpect(status().isForbidden());
    }


    @Test
    void addUsersToTeam_InvalidEmail_400() throws Exception {


        List<UserToTeamDTO> input = new ArrayList<>();
        input.add(new UserToTeamDTO("first", UUID.randomUUID()));

        String toSend = mapper.writeValueAsString(input);

        mockMvc.perform(post("/api/v1/admin/teams/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toSend))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("userEmail must be a valid email")));
    }


    @Test
    void addUsersToTeam_MissingTeamId_400() throws Exception {

        List<UserToTeamDTO> input = new ArrayList<>();
        input.add(new UserToTeamDTO("first", null));

        String toSend = mapper.writeValueAsString(input);

        mockMvc.perform(post("/api/v1/admin/teams/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toSend))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Missing teamId")));
    }


    @Test
    void addUsersToTeam_MissingEmail_400() throws Exception {

        mockMvc.perform(post("/api/v1/admin/teams/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"teamId\":\"fd2ffc9e-fa96-4acd-bca4-c2565de5f337\"}]"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Missing userEmail")));
    }

    @Test
    void addUsersToTeam_MalformedUUID_400() throws Exception {

        mockMvc.perform(post("/api/v1/admin/teams/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"userEmail\":\"email@email.com\",\"teamId\":\"NotAUUID\"}]"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Request does not match API requirements")));
    }


    @Test
    void addUsersToTeam_Bluesky_200() throws Exception {

        UUID teamID = UUID.randomUUID();

        when(teamRepository.findById(teamID)).thenReturn(Optional.of(getTeam().id(teamID).build()));
        when(userRepository.findByEmail("email@email.com")).thenReturn(Optional.of(getUser().email("emai@email.com").build()));

        List<UserToTeamDTO> input = new ArrayList<>();
        input.add(new UserToTeamDTO("email@email.com", teamID));

        String toSend = mapper.writeValueAsString(input);

        mockMvc.perform(post("/api/v1/admin/teams/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toSend))
                .andExpect(status().isOk());

        verify(userRepository, times(1)).save(any());
    }


    @Test
    void addUsersToTeam_MissingUser_Throws404() throws Exception {

        UUID teamID = UUID.randomUUID();

        when(teamRepository.findById(teamID)).thenReturn(Optional.of(getTeam().id(teamID).build()));
        when(userRepository.findByEmail("email@email.com")).thenReturn(Optional.empty());

        List<UserToTeamDTO> input = new ArrayList<>();
        input.add(new UserToTeamDTO("email@email.com", teamID));

        String toSend = mapper.writeValueAsString(input);

        mockMvc.perform(post("/api/v1/admin/teams/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toSend))
                .andExpect(status().isNotFound());
    }


    @Test
    void addUsersToTeam_MissingTeam_Throws404() throws Exception {

        UUID teamID = UUID.randomUUID();

        when(teamRepository.findById(teamID)).thenReturn(Optional.empty());
        when(userRepository.findByEmail("email@email.com")).thenReturn(Optional.of(getUser().email("email@email.com").build()));

        List<UserToTeamDTO> input = new ArrayList<>();
        input.add(new UserToTeamDTO("email@email.com", teamID));

        String toSend = mapper.writeValueAsString(input);

        mockMvc.perform(post("/api/v1/admin/teams/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toSend))
                .andExpect(status().isNotFound());
    }
}
