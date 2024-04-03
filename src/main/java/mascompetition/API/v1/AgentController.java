package mascompetition.API.v1;

import jakarta.validation.Valid;
import mascompetition.API.BaseController;
import mascompetition.BLL.AgentService;
import mascompetition.DTO.CreateAgentDTO;
import mascompetition.DTO.CreateAgentResponseDTO;
import mascompetition.Exception.ActionForbiddenException;
import mascompetition.Exception.AgentStorageException;
import mascompetition.Exception.BadInformationException;
import mascompetition.Exception.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Controller responsible for interaction with agents
 */
@RestController
@RequestMapping("/api/v1")
public class AgentController extends BaseController {

    @Autowired
    AgentService agentService;


    @PostMapping(value = "/agents")
    public ResponseEntity<CreateAgentResponseDTO> createAgent(
            @RequestPart("source") MultipartFile agentCode,
            @RequestPart("data") @Valid CreateAgentDTO createAgentDTO,
            BindingResult result) throws BadInformationException, ActionForbiddenException, AgentStorageException, EntityNotFoundException {

        validateEndpoint(result);

        UUID createdAgentID = agentService.createAgent(createAgentDTO, agentCode);

        return ResponseEntity.status(HttpStatus.CREATED).body(CreateAgentResponseDTO.builder()
                .agentID(createdAgentID)
                .nextRound(ZonedDateTime.now().plusMinutes(5))
                .build());
    }

}
