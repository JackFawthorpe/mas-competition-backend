package mascompetition.BLL;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Service responsible for dealing with the file system
 */
@Service
public class DirectoryService {

    /**
     * Saves the provided file at the given path
     *
     * @param inputStream The data to store in the given path
     * @param path        The path
     * @throws IOException Thrown if something goes wrong
     */
    public void saveFile(InputStream inputStream, Path path) throws IOException {
        Files.createDirectories(path.getParent());
        Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Deletes the file at the path
     *
     * @param path The path
     * @throws IOException Thrown if something goes wrong
     */
    public void deleteFile(Path path) throws IOException {
        Files.delete(path);
    }
}
