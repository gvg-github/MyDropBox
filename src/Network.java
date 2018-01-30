import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Network {
    private static final String DIR_PATH = "D:\\FilesDB";

    public static boolean saveFileOnDisk(String dir, FileMessage fm) {

        String pathWithoutName = DIR_PATH + "\\" + dir;
        String pathWithName = DIR_PATH + "\\" + dir + "\\" + fm.getName();
        if (verifyPath(pathWithoutName)) {
            try {
                Files.write(Paths.get(pathWithName), fm.getData(), StandardOpenOption.CREATE_NEW);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static boolean verifyPath(String path) {
//        Files.createDirectories()
        return true;
    }

    public static File[] getUserFileStructure(String user) {
        File file = new File(DIR_PATH + "\\" + user);
        return file.listFiles();
    }

    public static boolean makeDir(String nameDir) {
        boolean makeDir = false;
        String pathDir = DIR_PATH + "\\" + nameDir;
        try {
            Files.createDirectory(Paths.get(pathDir));
            makeDir = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return makeDir;
    }

}
