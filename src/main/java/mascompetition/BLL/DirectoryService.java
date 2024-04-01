package mascompetition.BLL;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
     * @param file The file
     * @param path The path
     * @throws IOException Thrown if something goes wrong
     */
    public void saveFile(MultipartFile file, Path path) throws IOException {
        Files.createDirectories(path.getParent());
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
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
