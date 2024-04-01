package mascompetition.unit.BLL;

import mascompetition.BLL.AgentService;
import mascompetition.BLL.AgentValidator;
import mascompetition.BLL.DirectoryService;
import mascompetition.BLL.UserService;
import mascompetition.BaseTestFixture;
import mascompetition.DTO.CreateAgentDTO;
import mascompetition.Entity.Agent;
import mascompetition.Entity.User;
import mascompetition.Exception.ActionForbiddenException;
import mascompetition.Exception.AgentStorageException;
import mascompetition.Exception.BadInformationException;
import mascompetition.Exception.EntityNotFoundException;
import mascompetition.Repository.AgentRepository;
import mascompetition.Repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AgentServiceTest extends BaseTestFixture {

    User currentUser;
    MockedStatic<Files> mockedFiles;
    @InjectMocks
    private AgentService agentService;

    @Mock
    private DirectoryService directoryService;
    @Mock
    private AgentRepository agentRepository;
    @Mock
    private UserService userService;
    @Mock
    private AgentValidator agentValidator;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void resetMocks() throws BadInformationException, ActionForbiddenException, EntityNotFoundException, IOException {
        currentUser = getUser()
                .team(getTeam().build())
                .build();

        lenient().doNothing().when(directoryService).saveFile(any(), any());

        lenient().when(agentValidator.validateAuthors(any())).thenReturn(List.of(currentUser));
        lenient().when(userService.getCurrentUser()).thenReturn(currentUser);
        lenient().when(agentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        lenient().when(userRepository.findByEmail(any())).thenReturn(Optional.of(currentUser));
    }

    @Test
    void createAgent_bluesky() throws BadInformationException, ActionForbiddenException, AgentStorageException, EntityNotFoundException {
        CreateAgentDTO createAgentDTO = CreateAgentDTO.builder()
                .versionNumber(1)
                .designTime(5)
                .name("DefaultName")
                .emails(List.of(currentUser.getEmail())).build();

        UUID response = agentService.createAgent(createAgentDTO, mock(MultipartFile.class));

        ArgumentCaptor<Agent> savedAgentCaptor = ArgumentCaptor.forClass(Agent.class);
        verify(agentRepository, times(1)).save(savedAgentCaptor.capture());

        Agent savedAgent = savedAgentCaptor.getValue();
        Assertions.assertEquals(createAgentDTO.getDesignTime(), savedAgent.getDesignTime());
        Assertions.assertEquals(createAgentDTO.getVersionNumber(), savedAgent.getVersionNumber());
        Assertions.assertEquals(createAgentDTO.getName(), savedAgent.getName());
        Assertions.assertEquals(response, savedAgent.getId());
    }

    @Test
    void createAgent_DirectoryServiceFails_agentStorageException() throws BadInformationException, ActionForbiddenException, AgentStorageException, EntityNotFoundException, IOException {
        CreateAgentDTO createAgentDTO = CreateAgentDTO.builder()
                .versionNumber(1)
                .designTime(5)
                .name("DefaultName")
                .emails(List.of(currentUser.getEmail())).build();

        doThrow(new IOException("Failed to save")).when(directoryService).saveFile(any(), any());

        Assertions.assertThrows(AgentStorageException.class, () -> agentService.createAgent(createAgentDTO, mock(MultipartFile.class)));

        verify(agentRepository, times(0)).save(any());
    }


    @Test
    void createAgent_DatabaseFails_DirectoryServiceReverts() throws IOException, BadInformationException, ActionForbiddenException, AgentStorageException, EntityNotFoundException {
        CreateAgentDTO createAgentDTO = CreateAgentDTO.builder()
                .versionNumber(1)
                .designTime(5)
                .name("DefaultName")
                .emails(List.of(currentUser.getEmail())).build();

        doThrow(new RuntimeException("Failed to save")).when(agentRepository).save(any());

        Assertions.assertThrows(AgentStorageException.class, () -> agentService.createAgent(createAgentDTO, mock(MultipartFile.class)));

        ArgumentCaptor<Path> savePath = ArgumentCaptor.forClass(Path.class);
        ArgumentCaptor<Path> deletePath = ArgumentCaptor.forClass(Path.class);

        verify(directoryService, times(1)).deleteFile(savePath.capture());
        verify(directoryService, times(1)).saveFile(any(), deletePath.capture());

        Assertions.assertEquals(savePath.getValue(), deletePath.getValue());
    }

}
