package mascompetition.BLL;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import mascompetition.Entity.Agent;
import mascompetition.Exception.AgentParseException;
import mascompetition.Exception.LoadAgentException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

@Service
public class AgentParser {

    public CompilationUnit getCompilationUnit(Path path, Agent agent) throws LoadAgentException, AgentParseException {
        File file = new File(path.toUri());
        try (FileInputStream in = new FileInputStream(file)) {
            return StaticJavaParser.parse(in);
        } catch (IOException e) {
            throw new LoadAgentException(String.format("Failed to load agent from %s with Exception %s", path, e.getMessage()));
        } catch (ParseProblemException e) {
            throw new AgentParseException(String.format("Agent %s failed to be parsed", agent));
        }
    }

}
