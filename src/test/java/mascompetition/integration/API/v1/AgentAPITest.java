package mascompetition.integration.API.v1;

import mascompetition.DTO.AgentListDTO;
import mascompetition.DTO.CreateAgentDTO;
import mascompetition.Entity.Agent;
import mascompetition.Entity.GlickoRating;
import mascompetition.Entity.Team;
import mascompetition.integration.IntegrationTestFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.test.context.support.WithAnonymousUser;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AgentAPITest extends IntegrationTestFixture {

    @ParameterizedTest
    @ValueSource(strings = {"sixlet", "CAPTIALS", "ssssssssssssssssssssssssssssssss"})
    void createAgent_bluesky_201(String validName) throws Exception {

        currentUser.setTeam(getTeam().build());

        MockMultipartFile agentCodeFile = new MockMultipartFile("source", "bluesky.java", MediaType.MULTIPART_FORM_DATA_VALUE, getClass().getResourceAsStream("/integration/agents/bluesky.java"));
        String toSend = mapper.writeValueAsString(CreateAgentDTO.builder()
                .emails(List.of(currentUser.getEmail()))
                .name(validName)
                .versionNumber(1)
                .designTime(5)
                .build());

        mockMvc.perform(multipart("/api/v1/agents")
                        .part(new MockPart("data", "", toSend.getBytes(), MediaType.APPLICATION_JSON))
                        .file(agentCodeFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());

        verify(directoryService, times(1)).saveFile(any(), any());
        verify(agentRepository, times(1)).save(any());
    }


    @Test
    @WithAnonymousUser
    void createAgent_notLoggedIn_401() throws Exception {

        MockMultipartFile agentCodeFile = new MockMultipartFile("source", "bluesky.java", MediaType.MULTIPART_FORM_DATA_VALUE, getClass().getResourceAsStream("/integration/agents/bluesky.java"));
        String toSend = mapper.writeValueAsString(CreateAgentDTO.builder()
                .emails(List.of(currentUser.getEmail()))
                .name("bluesky")
                .versionNumber(1)
                .designTime(5)
                .build());

        mockMvc.perform(multipart("/api/v1/agents")
                        .part(new MockPart("data", "", toSend.getBytes(), MediaType.APPLICATION_JSON))
                        .file(agentCodeFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is3xxRedirection());
    }


    @Test
    void createAgent_missingAuthors_400() throws Exception {

        currentUser.setTeam(getTeam().build());

        MockMultipartFile agentCodeFile = new MockMultipartFile("source", "bluesky.java", MediaType.MULTIPART_FORM_DATA_VALUE, getClass().getResourceAsStream("/integration/agents/bluesky.java"));
        String toSend = mapper.writeValueAsString(CreateAgentDTO.builder()
                .name("bluesky")
                .versionNumber(1)
                .designTime(5)
                .build());

        mockMvc.perform(multipart("/api/v1/agents")
                        .part(new MockPart("data", "", toSend.getBytes(), MediaType.APPLICATION_JSON))
                        .file(agentCodeFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("You must provide a list of emails from your team that authored the agent")));
    }


    @Test
    void createAgent_missingVersionNumber_400() throws Exception {

        currentUser.setTeam(getTeam().build());

        MockMultipartFile agentCodeFile = new MockMultipartFile("source", "bluesky.java", MediaType.MULTIPART_FORM_DATA_VALUE, getClass().getResourceAsStream("/integration/agents/bluesky.java"));
        String toSend = mapper.writeValueAsString(CreateAgentDTO.builder()
                .emails(List.of(currentUser.getEmail()))
                .name("bluesky")
                .designTime(5)
                .build());

        mockMvc.perform(multipart("/api/v1/agents")
                        .part(new MockPart("data", "", toSend.getBytes(), MediaType.APPLICATION_JSON))
                        .file(agentCodeFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("versionNumber is a required field")));
    }


    @Test
    void createAgent_missingDesignTime_400() throws Exception {

        currentUser.setTeam(getTeam().build());

        MockMultipartFile agentCodeFile = new MockMultipartFile("source", "bluesky.java", MediaType.MULTIPART_FORM_DATA_VALUE, getClass().getResourceAsStream("/integration/agents/bluesky.java"));
        String toSend = mapper.writeValueAsString(CreateAgentDTO.builder()
                .emails(List.of(currentUser.getEmail()))
                .name("bluesky")
                .versionNumber(5)
                .build());

        mockMvc.perform(multipart("/api/v1/agents")
                        .part(new MockPart("data", "", toSend.getBytes(), MediaType.APPLICATION_JSON))
                        .file(agentCodeFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("designTime is a required field")));
    }


    @Test
    void createAgent_noName_400() throws Exception {

        currentUser.setTeam(getTeam().build());

        MockMultipartFile agentCodeFile = new MockMultipartFile("source", "bluesky.java", MediaType.MULTIPART_FORM_DATA_VALUE, getClass().getResourceAsStream("/integration/agents/bluesky.java"));
        String toSend = mapper.writeValueAsString(CreateAgentDTO.builder()
                .emails(List.of(currentUser.getEmail()))
                .versionNumber(5)
                .designTime(5)
                .build());

        mockMvc.perform(multipart("/api/v1/agents")
                        .part(new MockPart("data", "", toSend.getBytes(), MediaType.APPLICATION_JSON))
                        .file(agentCodeFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("name is a required field")));
    }


    @ParameterizedTest
    @ValueSource(strings = {"aaaaa", "aaaaaa1", "aaaa!@", "sssssssssssssssssssssssssssssssss"})
    void createAgent_InvalidName_400(String invalidName) throws Exception {

        currentUser.setTeam(getTeam().build());

        MockMultipartFile agentCodeFile = new MockMultipartFile("source", "bluesky.java", MediaType.MULTIPART_FORM_DATA_VALUE, getClass().getResourceAsStream("/integration/agents/bluesky.java"));
        String toSend = mapper.writeValueAsString(CreateAgentDTO.builder()
                .emails(List.of(currentUser.getEmail()))
                .name("bluesky")
                .versionNumber(5)
                .designTime(5)
                .name(invalidName)
                .build());

        mockMvc.perform(multipart("/api/v1/agents")
                        .part(new MockPart("data", "", toSend.getBytes(), MediaType.APPLICATION_JSON))
                        .file(agentCodeFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("name")));
    }


    @Test
    void getAgents_Bluesky_200() throws Exception {
        Team team = getTeam().build();
        GlickoRating glickoRating = GlickoRating.newRating();
        Agent agent = getAgent().team(team).glickoRating(glickoRating).build();

        AgentListDTO expected = AgentListDTO.builder()
                .agentName(String.format("%s v%s", agent.getName(), agent.getVersionNumber()))
                .teamName(team.getName())
                .agentId(agent.getId())
                .teamId(team.getId())
                .rating(1500)
                .status("UNVALIDATED")
                .build();
        when(agentRepository.findAllByOrderByRatingDesc()).thenReturn(List.of(agent));

        mockMvc.perform(get("/api/v1/agents"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(expected))));
    }


}
