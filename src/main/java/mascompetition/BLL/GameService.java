package mascompetition.BLL;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Name;
import mascompetition.Entity.Agent;
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
import java.util.Optional;

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

    @Value("${engineStoragePath}")
    private String enginePath;

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
    public List<Integer> runGame(List<Agent> agents) {
        try {
            logger.info("Running game with the following agents: {} {} {} {}",
                    agents.get(0).getId(),
                    agents.get(1).getId(),
                    agents.get(2).getId(),
                    agents.get(3).getId());
            List<Path> agentPaths = agents.stream().map(agent -> agentService.getAgentPath(agent)).toList();

            loadPlayers(agentPaths);
            return runProcess();

        } catch (Exception e) {
            logger.error("Game failed to run with agents: {} {} {} {} with exception {}",
                    agents.get(0).getId(),
                    agents.get(1).getId(),
                    agents.get(2).getId(),
                    agents.get(3).getId(),
                    e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Loads validated agents into the game's player directory
     *
     * @param paths The list of agents to load
     * @throws LoadAgentException Thrown when there is an issue loading the agent into the system
     */
    private void loadPlayers(List<Path> paths) throws LoadAgentException {
        for (int i = 0; i < paths.size(); i++) {
            Path path = paths.get(i);
            CompilationUnit compilationUnit;
            File file = new File(path.toUri());

            try (FileInputStream in = new FileInputStream(file)) {
                compilationUnit = StaticJavaParser.parse(in);
            } catch (IOException e) {
                throw new LoadAgentException(String.format("Failed to load agent from %s with Exception %s", path, e.getMessage()));
            }

            compilationUnit.setPackageDeclaration(new PackageDeclaration(new Name("api.agent")));
            String className = extractFileName(path.toString());
            Optional<ClassOrInterfaceDeclaration> oClass = compilationUnit.getClassByName(className);
            if (oClass.isEmpty()) {
                throw new LoadAgentException("No class found within the file");
            }
            oClass.get().setName(String.format("Player_%d", i));
            File outputDir = new File(getPlayerPath(i));

            // Save the modified file
            try (FileOutputStream out = new FileOutputStream(outputDir)) {
                out.write(compilationUnit.toString().getBytes());
            } catch (IOException e) {
                throw new LoadAgentException(String.format("Failed to save agent to %s with Exception %s", outputDir, e.getMessage()));
            }
            logger.info("Loaded {} into Player_{}", paths.get(i), i);
        }
    }

    /**
     * Runs a game between the four agents
     *
     * @return The scores of each of the agents
     */
    private List<Integer> runProcess() throws IOException, InterruptedException {
        logger.info("Loading engine");
        ProcessBuilder builder = new ProcessBuilder("java", "-jar", enginePath,
                getPlayerPath(0),
                getPlayerPath(1),
                getPlayerPath(2),
                getPlayerPath(3)
        );
        Process process = builder.start();
        logger.info("Starting engine");

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        List<Integer> integers = new ArrayList<>();
        String line;
        for (int i = 0; i < 4; i++) {
            line = reader.readLine();
            integers.add(Integer.parseInt(line.trim()));
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            logger.error("Engine failed with exit code {}", exitCode);
        } else {
            logger.info("Engine ran successfully");
        }
        return integers;
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

    /**
     * Gets the path to store players in
     *
     * @param player The player number
     * @return String representation of the directory to load the player into
     */
    private String getPlayerPath(int player) {
        return String.format("%splayers/Player_%d.java", agentDir, player);
    }
}
