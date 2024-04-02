package mascompetition.integration.API.v1;

import mascompetition.Entity.Team;
import mascompetition.integration.IntegrationTestFixture;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TeamAPITest extends IntegrationTestFixture {
    @Test
    void getTeam_NoTeamID_400() throws Exception {
        mockMvc.perform(get("/api/v1/teams/"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void getTeam_notAuthenticated_401() throws Exception {
        mockMvc.perform(get("/api/v1/teams/" + UUID.randomUUID()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void getTeam_teamNotFound_400() throws Exception {
        when(teamRepository.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/teams/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTeam_bluesky_200() throws Exception {
        Team team = getTeam().build();
        when(teamRepository.findById(any())).thenReturn(Optional.ofNullable(team));

        mockMvc.perform(get("/api/v1/teams/" + team.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", equalTo(team.getName())))
                .andExpect(jsonPath("id", equalTo(team.getId().toString())));

        verify(teamRepository, times(1)).findById(team.getId());
    }


}
