package mascompetition.BLL;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import mascompetition.Entity.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
     */
    public List<Integer> runGame(List<Agent> agents) {
        try {
            logger.info("Running game with the following agents: {} {} {} {}", agents.get(0).getId(), agents.get(1).getId(), agents.get(2).getId(), agents.get(3).getId());
            List<Path> agentPaths = agents.stream().map(agent -> agentService.getAgentPath(agent)).toList();

            loadPlayers(agentPaths);
            return runProcess();
        } catch (Exception e) {
            logger.error("Game failed to run with agents: {} {} {} {} with exception {}", agents.get(0).getId(), agents.get(1).getId(), agents.get(2).getId(), agents.get(3).getId(), e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    private void loadPlayers(List<Path> paths) throws Exception {
        for (int i = 0; i < paths.size(); i++) {

            Path path = paths.get(i);

            File file = new File(path.toUri());
            FileInputStream in = new FileInputStream(file);
            CompilationUnit cu = StaticJavaParser.parse(in);
            in.close();

            String className = extractFileName(path.toString());

            // TODO: Verify these .get's are safe

            // Change the name of the class
            cu.getPackageDeclaration().get().setName("api.agent");
            cu.getClassByName(className).get().setName(String.format("Player_%d", i));

            File output = new File(getPlayerPath(i));

            // Save the modified file
            try (FileOutputStream out = new FileOutputStream(output)) {
                out.write(cu.toString().getBytes());
            }
        }
    }

    /**
     * Runs a game between the four agents
     *
     * @return The scores of each of the agents
     */
    private List<Integer> runProcess() throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder("java", "-jar", enginePath,
                getPlayerPath(0),
                getPlayerPath(1),
                getPlayerPath(2),
                getPlayerPath(3)
        );
        Process process = builder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        List<Integer> integers = new ArrayList<>();

        String line;
        for (int i = 0; i < 4; i++) {
            line = reader.readLine();
            integers.add(Integer.parseInt(line.trim()));
        }
        int exitCode = process.waitFor();
        return integers;
    }

    private String extractFileName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }

        // Find the last index of file separator
        int lastIndex = filePath.lastIndexOf(File.separator);

        // If file separator not found, return the entire string
        if (lastIndex == -1) {
            return filePath;
        }

        // Extract the file name
        String fileName = filePath.substring(lastIndex + 1);

        // Find the last index of the dot (file extension)
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            // If file extension not found, return the entire file name
            return fileName;
        } else {
            // Remove the extension and return
            return fileName.substring(0, dotIndex);
        }
    }

    private String getPlayerPath(int player) {
        return String.format("%splayers/Player_%d.java", agentDir, player);
    }
}
