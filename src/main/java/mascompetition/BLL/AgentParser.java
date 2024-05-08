package mascompetition.BLL;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import mascompetition.Entity.Agent;
import mascompetition.Exception.AgentParseException;
import mascompetition.Exception.LoadAgentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The AgentParser is responsible for functionality pertaining to the parsing and reading of user submitted agent files
 */
@Service
public class AgentParser {

    Logger logger = LoggerFactory.getLogger(AgentParser.class);

    /**
     * Checks whether the submitted agent has used any illegal methods within its source
     * This includes checking imports as well as the use of System / Compiler / Threading
     *
     * @param compilationUnit The agent source code to validate
     * @return true if the agent is valid
     */
    public boolean validateSourceCode(CompilationUnit compilationUnit) {
        return validateImports(compilationUnit) && validateLangUsage(compilationUnit);
    }

    /**
     * Parses the imports of the source code and checks they are all allowed
     *
     * @param compilationUnit The source code of the agent
     * @return True if the agent has no invalid imports
     */
    private boolean validateImports(CompilationUnit compilationUnit) {
        ImportVisitor importVisitor = new ImportVisitor();
        compilationUnit.accept(importVisitor, null);
        return importVisitor.isValid();
    }

    /**
     * Checks if the usage of the java.lang (auto-imported) package is safe
     *
     * @param compilationUnit The source code of the agent
     * @return True if none of the usages are illegal
     */
    private boolean validateLangUsage(CompilationUnit compilationUnit) {
        AtomicBoolean isValid = new AtomicBoolean(true);
        compilationUnit.findAll(Statement.class).forEach(method -> {
            if (method.toString().contains("System")) {
                logger.warn("Detected Usage of System");
                isValid.set(false);
            } else if (method.toString().contains("Compiler")) {
                logger.warn("Detected Usage of Compiler");
                isValid.set(false);
            } else if (method.toString().contains("Runtime")) {
                logger.warn("Detected Usage of Runtime");
                isValid.set(false);
            }
        });

        compilationUnit.findAll(ObjectCreationExpr.class).forEach(decl -> {
            if (decl.toString().contains("Process")) {
                logger.warn("Detected Usage of Process");
                isValid.set(false);
            } else if (decl.toString().contains("Thread")) {
                logger.warn("Detected Usage of Thread");
                isValid.set(false);
            }
        });

        return isValid.get();
    }

    /**
     * Opens the file at the path and turns it into a form that the JavaParser library can interpret
     *
     * @param path  The path of the agent
     * @param agent The agent the file pertains to
     * @return The compilation unit oif the source file
     * @throws LoadAgentException  Thrown if the agent cannot be loaded from the file
     * @throws AgentParseException Thrown if the source code is invalid (won't compile)
     */
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

    /**
     * Class for handling import checking
     */
    private static class ImportVisitor extends VoidVisitorAdapter<Void> {

        private final List<String> whitelist;
        private final List<String> blacklist;
        Logger logger = LoggerFactory.getLogger(ImportVisitor.class);
        private boolean isValid;

        ImportVisitor() {
            this.isValid = true;
            whitelist = new ArrayList<>();
            blacklist = new ArrayList<>();
            whitelist.add("java.util.Collection");
            whitelist.add("java.util.Comparator");
            whitelist.add("java.util.Iterator");
            whitelist.add("java.util.List");
            whitelist.add("java.util.ListIterator");
            whitelist.add("java.util.Map");
            whitelist.add("java.util.MapEntry");
            whitelist.add("java.util.Set");
            whitelist.add("java.util.Spliterator");
            whitelist.add("java.util.ArrayList");
            whitelist.add("java.util.Collections");
            whitelist.add("java.util.HashMap");
            whitelist.add("java.util.HashSet");
            whitelist.add("java.util.Optional");
            whitelist.add("java.util.Random");
            whitelist.add("api");
            blacklist.add("api.agent");
        }

        public boolean isValid() {
            return isValid;
        }

        /**
         * This will be called every time an import is detected within the compilation unit
         *
         * @param importDeclaration an import statement
         * @param arg               No clue
         */
        @Override
        public void visit(ImportDeclaration importDeclaration, Void arg) {
            if (!isValid) return;
            String importName = importDeclaration.getNameAsString();
            for (String invalidImport : blacklist) {
                if (importName.contains(invalidImport)) {
                    logger.warn("Detected use of blacklisted import {}", invalidImport);
                    this.isValid = false;
                    return;
                }
            }
            for (String validImport : whitelist) {
                if (importName.startsWith(validImport)) {
                    super.visit(importDeclaration, arg);
                    return;
                }
            }
            logger.warn("Detected use of illegal import {}", importName);
            this.isValid = false;
        }
    }
}
