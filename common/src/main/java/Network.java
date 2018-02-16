import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Network {

    public static AnswerMessage sendFolderMessage(Socket clientSocket, String folderName, boolean create, boolean delete, String newFolderName) {

        FolderMessage fdm = new FolderMessage(folderName, create, delete, newFolderName);
        return SendMessage(clientSocket, fdm);
    }

    public static AnswerMessage sendFileMessage(Socket clientSocket, String folderName, FileActionEnum type, String newName, String tecPath) {

        FileMessage fm = new FileMessage(folderName, type, newName, tecPath);
        return SendMessage(clientSocket, fm);

    }

    public static void sendAnswerMessage(Socket clientSocket, String user, boolean status, String msg) {
        File[] userFiles = null;
        int userSize = 0;
        if (user != null) {
            userFiles = WorkWithFiles.getUserFileStructure(user);
            if (userFiles == null){
                userSize = 0;
            }else{
                userSize = WorkWithFiles.getUserFileSize(user, userFiles);
            }
        }

        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            AnswerMessage aMsg = new AnswerMessage(status, msg, userFiles, userSize);
            oos.writeObject(aMsg);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static AnswerMessage SendMessage(Socket clientSocket, FolderMessage fdm) {
        AnswerMessage ansMsg = null;
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            oos.writeObject(fdm);
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
            Object obj = ois.readObject();
            if (obj instanceof AnswerMessage) {
                ansMsg = (AnswerMessage) obj;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return ansMsg;

    }

    private static AnswerMessage SendMessage(Socket clientSocket, FileMessage fdm) {
        AnswerMessage ansMsg = null;
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            oos.writeObject(fdm);
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
            Object obj = ois.readObject();
            if (obj instanceof AnswerMessage) {
                ansMsg = (AnswerMessage) obj;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return ansMsg;

    }

    public static AnswerMessage sendFile(File file, String filename, Socket socket) throws IOException, ClassNotFoundException {
        AnswerMessage ansMsg = null;
        long fileSize = Files.size(Paths.get(file.getAbsolutePath()));
        if (fileSize <= Consts.FILE_SIZE) {
            TransferFileMessage trFm = new TransferFileMessage(filename, file.getAbsolutePath(), fileSize, Files.readAllBytes(Paths.get(file.getAbsolutePath())), true, true);

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(trFm);
            oos.flush();

            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Object obj = ois.readObject();
            if (obj instanceof AnswerMessage) {
                ansMsg = (AnswerMessage) obj;
            }

        } else {

            FileInputStream fis = new FileInputStream(file);
            ArrayList<byte[]> aList = new ArrayList<>();

            int count = (int) (fileSize / Consts.FILE_SIZE);
            int lastSize = (int) (fileSize - (Consts.FILE_SIZE * count));
            int startPos = 0;
            int readByte = 0;
            for (int i = 0; i <= count; i++) {

                byte[] x = {};
                int fragmentLength = 0;
                if (i == count) {
                    x = new byte[lastSize];
                    fragmentLength = fis.available();
                } else {
                    x = new byte[Consts.FILE_SIZE];
                    fragmentLength = Consts.FILE_SIZE;
                }
                readByte = fis.read(x);
                aList.add(x);
                if (readByte > 0) {
                    readByte = 0;
                    startPos += Consts.FILE_SIZE;
                }
            }

            for (int i = 0; i < aList.size(); i++) {
                boolean end = false;
                if (aList.size() - 1 == i) {
                    end = true;
                }
                TransferFileMessage trFm = new TransferFileMessage(filename, file.getAbsolutePath(), fileSize, aList.get(i), true, end);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(trFm);
                oos.flush();
            }
            fis.close();
            aList.clear();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Object obj = ois.readObject();
            if (obj instanceof AnswerMessage) {
                ansMsg = (AnswerMessage) obj;
            }
        }
        return ansMsg;
    }

    public static boolean getFile(TransferFileMessage trFm, String fileName, String pathToFile, Socket socket) throws IOException, ClassNotFoundException {

        boolean gotIt = false;
        File file = new File(pathToFile);
//        String fileName = trFm.getName();
        long size = trFm.getSize();
        ArrayList<byte[]> aList = new ArrayList<>();
        aList.add(trFm.getData());
        while (!trFm.isEndOfFile()) {
//                                sendAnswerMessage(true, null);
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            trFm = (TransferFileMessage) ois.readObject();
            aList.add(trFm.getData());

        }

        if (aList.size() > 0) {
            FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
            for (int i = 0; i < aList.size(); i++) {
                fos.write(aList.get(i));
                fos.flush();
            }
            fos.close();
        }
        aList.clear();

        if (file.exists()) {
            long fileSize = Files.size(Paths.get(file.getAbsolutePath()));
            if (fileSize == size) {
                gotIt = true;
            }
        }
        return gotIt;
    }

    public static boolean getFile(String pathToFile, Socket socket, String user) throws IOException, ClassNotFoundException {

        boolean getFile = false;
        File file = new File(pathToFile);
        if (file.exists()){
            AnswerMessage ansMsg = sendFile(file, file.getName(), socket);
            getFile = ansMsg.isYes();
        }
        return getFile;
    }

}
