package mascompetition.BLL;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.expr.Name;
import mascompetition.Entity.Agent;
import mascompetition.Entity.AgentStatus;
import mascompetition.Exception.AgentParseException;
import mascompetition.Exception.EngineFailureException;
import mascompetition.Exception.LoadAgentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for handling the execution of a single game
 */
@Service
public class GameService {

    Logger logger = LoggerFactory.getLogger(GameService.class);

    @Autowired
    private AgentService agentService;

    @Value("${agentStoragePath}")
    private String agentDir;

    @Value("${enginePath}")
    private String enginePath;

    @Autowired
    private AgentParser agentParser;

    @Autowired
    private DirectoryService directoryService;

    /**
     * Runs a game of 4 agents and updates their ratings
     * Rules:
     * - The agents must all be unique
     * - The agents must not be null
     * - There must be exactly 4
     *
     * @param agents The list of agents to play
     * @return If successful, the amount of points each player scored during the game
     * Else it returns an empty list
     */
    public List<Integer> runGame(List<Agent> agents) throws IOException, LoadAgentException, EngineFailureException, InterruptedException {
        try {
            logger.info("Running game with the following agents: {} {} {} {}",
                    agents.get(0).getId(),
                    agents.get(1).getId(),
                    agents.get(2).getId(),
                    agents.get(3).getId());
            loadPlayers(agents);
            return runProcess();
        } catch (EngineFailureException e) {
            handleEngineFailure(e.getExitCode(), agents);
            throw e;
        }
    }

    /**
     * Loads validated agents into the game's player directory
     *
     * @param agents The list of agents to load
     * @throws LoadAgentException Thrown when there is an issue loading the agent into the system
     */
    private void loadPlayers(List<Agent> agents) throws LoadAgentException {
        for (int i = 0; i < agents.size(); i++) {
            loadPlayer(agents.get(i), i);
        }
    }

    /**
     * Runs a game between the four agents
     *
     * @return The scores of each of the agents
     */
    private List<Integer> runProcess() throws IOException, InterruptedException, EngineFailureException {
        logger.info("Loading engine");
        ProcessBuilder builder = new ProcessBuilder("java", "-jar", enginePath,
                getPlayerPath(0),
                getPlayerPath(1),
                getPlayerPath(2),
                getPlayerPath(3)
        );

        logger.info("Starting engine");
        Process process = builder.start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new EngineFailureException(exitCode);
        }

        logger.info("Engine ran successfully");

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        List<Integer> integers = new ArrayList<>();
        String line;
        for (int i = 0; i < 4; i++) {
            line = reader.readLine();
            integers.add(Integer.parseInt(line.trim()));
        }
        return integers;
    }

    private void handleEngineFailure(int exitCode, List<Agent> agents) {
        switch (exitCode) {
            case 2, 3 -> logger.error("Internal error occurred within the engine");
            case 4, 5, 6, 7 -> {
                logger.error("Compilation failed within the engine (This should have been caught before the engine)");
                logger.error("Compilation failed for player {}", exitCode - 4);
            }
            case 8, 9, 10, 11 ->
                    logger.error("Loading class unexpectedly failed after compilation for player {}", exitCode - 8);
            case 16, 17, 18, 19 -> {
                logger.warn("Player {} doesn't implement the interface correctly", exitCode - 16);
                agentService.setAgentStatus(agents.get(exitCode - 16), AgentStatus.INVALID_SUBMISSION);
            }
            case 29, 30, 31, 32 -> {
                logger.warn("Player {} timed out on their turn", exitCode - 29);
                agentService.setAgentStatus(agents.get(exitCode - 29), AgentStatus.TIMED_OUT);
            }
            case 33, 34, 35, 36 -> {
                logger.warn("Player {} made an illegal move on their turn", exitCode - 33);
                agentService.setAgentStatus(agents.get(exitCode - 33), AgentStatus.ILLEGAL_MOVE);
            }
            case 37, 38, 39, 40 -> {
                logger.warn("Player {} ran out of memory on their turn", exitCode - 37);
                agentService.setAgentStatus(agents.get(exitCode - 37), AgentStatus.OUT_OF_MEMORY);
            }
            default -> logger.error("Unmapped error code received {}", exitCode);

        }
    }

    private void loadPlayer(Agent agent, int playerNumber) throws LoadAgentException {
        Path path = agentService.getAgentPath(agent);
        CompilationUnit compilationUnit;

        // Fetch the compilation unit for the agent
        try {
            compilationUnit = agentParser.getCompilationUnit(path, agent);
        } catch (AgentParseException e) {
            logger.warn("Failed to parse agent {} with exception {}", agent.getId(), e.getMessage());
            agentService.setAgentStatus(agent, AgentStatus.INVALID_SUBMISSION);
            throw new LoadAgentException(String.format("Failed to parse agent %s", agent.getId()));
        }

        compilationUnit.setPackageDeclaration(new PackageDeclaration(new Name("api.agent")));
        String className = extractFileName(path.toString());
        compilationUnit.getClassByName(className).get().setName(String.format("Player_%d", playerNumber));

        // Save the modified file
        try {
            InputStream toSave = new ByteArrayInputStream(compilationUnit.toString().getBytes());
            directoryService.saveFile(toSave, Path.of(getPlayerPath(playerNumber)));
        } catch (IOException e) {
            throw new LoadAgentException(String.format("Failed to save agent to %s with Exception %s",
                    getPlayerPath(playerNumber),
                    e.getMessage()));
        }
        logger.info("Loaded {} into Player_{}", path, playerNumber);
    }

    /**
     * Gets the path to store players in
     *
     * @param player The player number
     * @return String representation of the directory to load the player into
     */
    private String getPlayerPath(int player) {
        return String.format("%splayers/Player_%d.java", agentDir, player);
    }

    /**
     * Gets the filename / classname from the agent from the path that is provided
     *
     * @param filePath The path of the agent
     * @return The expected name of the agent
     */
    private static String extractFileName(String filePath) {
        Path path = Paths.get(filePath);
        Path fileNamePath = path.getFileName();
        String fileName = fileNamePath.toString();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex != -1) {
            return fileName.substring(0, lastDotIndex);
        } else {
            return fileName;
        }
    }
}
