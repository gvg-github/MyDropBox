
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Network {
    public static final String DIR_PATH = "D:\\FilesDB\\";

    public static String getFullPath(String tecPath) {
        return new String(DIR_PATH + tecPath);
    }

    public static boolean saveFileOnDisk(String dir, FileMessage fm) {
        try {
            Files.write(Paths.get(dir), fm.getData(), StandardOpenOption.CREATE_NEW);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean verifyPath(String path) {
        File file = new File(DIR_PATH + path);
        if (file.exists() && file.isDirectory()) return true;
        return false;
    }

    public static File[] getUserFileStructure(String user) {
        File file = new File(DIR_PATH + user);
        return file.listFiles();
    }

    public static boolean makeDir(String nameDir) {
        boolean makeDir = false;
        String pathDir = DIR_PATH + nameDir;
        try {
            Files.createDirectory(Paths.get(pathDir));
            makeDir = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return makeDir;
    }

    public static File getFileOnServer(String path) {
        File file = new File(path);
        if (file.exists() && file.isFile()) return file;
        return null;
    }

    public static boolean deleteFileOnServer(String path) {
        File file = new File(path);
        if (file.exists() && file.isFile()) return file.delete();
        return false;
    }

    public static boolean renameFileOnServer(File file, String newName) {
        String tecName = file.getAbsolutePath();
        StringBuilder nameBuilder = new StringBuilder(tecName);

        int index1 = tecName.lastIndexOf("\\");
        int index2 = tecName.lastIndexOf(".");
        if (index2 != -1) {
            nameBuilder.replace(index1 + 1, index2, newName);
        } else {
            nameBuilder.replace(index1 + 1, tecName.length(), newName);
        }
        return file.renameTo(new File(nameBuilder.toString()));
    }

    public static boolean transferFileOnServer(File file, String newName) {
        String tecName = file.getAbsolutePath();
        int index1 = tecName.lastIndexOf("\\");
        StringBuilder nameBuilder = new StringBuilder(DIR_PATH + newName);
        nameBuilder.append(tecName.substring(index1));
        return file.renameTo(new File(nameBuilder.toString()));
    }

    public static boolean deleteDirOnServer(String nameDir) {
        File file = new File(nameDir);
        if (file.exists() && file.isDirectory() && file.listFiles().length == 0) return file.delete();
        return false;
    }

    public static File getFolderOnServer(String path) {
        File file = new File(path);
        if (file.exists() && file.isDirectory()) return file;
        return null;
    }

//    public static boolean renameFolderOnServer(File file, String newName) {
//
//        String tecName = file.getAbsolutePath();
//        StringBuilder nameBuilder = new StringBuilder(tecName);
//
//        int index1 = tecName.lastIndexOf("\\");
//        int index2 = tecName.lastIndexOf(".");
//
//        if (index2 != -1) {
//            nameBuilder.replace(index1 + 1, index2, newName);
//        } else {
//            nameBuilder.replace(index1 + 1, tecName.length(), newName);
//        }
//
//        return file.renameTo(new File(nameBuilder.toString()));
//    }
}
