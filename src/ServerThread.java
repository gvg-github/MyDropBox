
import javax.crypto.SecretKey;
import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ServerThread implements Runnable {

    private MultiThreadServer mtSever;
    private int port;
    private JTextArea sArea;

    private Socket client;
    private String user;
    private BD bd;
    private SecretKey threadKey;
    private Security threadSecurity;
    private HashMap<Socket, Long> clientsTime;
    private boolean isTimeout;

    public ServerThread(MultiThreadServer mtSever, Socket client, JTextArea sArea, int port, BD bd, HashMap<Socket, Long> clientsTime) {
        this.port = port;
        this.sArea = sArea;
        this.client = client;
        this.mtSever = mtSever;
        this.bd = bd;
        this.clientsTime = clientsTime;
//        isTimeout = false;
    }

    @Override
    public void run() {

        Network.sendAnswerMessage(client, null, true, Consts.formatForDate.format(new Date()) + ". Socket ready!");
        try {
            while (!client.isClosed()) {
                try {
//                    sendAnswerMessage(true, formatForDate.format(new Date()) + ". Socket ready!");
                    ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                    Object obj = ois.readObject();
                    if (obj instanceof TransferFileMessage) {
                        handleTransferFileMessage((TransferFileMessage) obj);
                    }
                    if (obj instanceof FileMessage) {
                        handleFileMessage((FileMessage) obj);
//                        FileMessage fm = (FileMessage) obj;
//                        String pathToFile = WorkWithFiles.DIR_PATH + fm.getName();
//                        if (fm.getData() == null) {
//                            if (fm.isDelete()) {
//                                if (WorkWithFiles.deleteFileOnServer(pathToFile)) {
//                                    sendAnswerMessage(true, Consts.formatForDate.format(new Date()) + ". File " + fm.getName() + " deleted!");
//                                } else {
//                                    sendAnswerMessage(false, Consts.formatForDate.format(new Date()) + ". Error when delete file " + fm.getName() + "...");
//                                }
//                            } else if (fm.isRefresh()) {
//                            } else if (fm.getNewName() != null && fm.getTecPath() == null) {
//                                File tecFile = WorkWithFiles.getFileOnServer(pathToFile);
//                                if (tecFile != null) {
//                                    if (WorkWithFiles.renameFileOnServer(tecFile, fm.getNewName())) {
//                                        sendAnswerMessage(true, Consts.formatForDate.format(new Date()) + ". File " + fm.getName() + "  renamed!");
//                                    } else {
//                                        sendAnswerMessage(false, Consts.formatForDate.format(new Date()) + ". Error when renamed file " + fm.getName() + "...");
//                                    }
//
//                                } else {
//                                    sendAnswerMessage(false, Consts.formatForDate.format(new Date()) + ". File " + fm.getName() + " not found!");
//                                }
//
//                            } else if (fm.getNewName() != null && fm.getTecPath() != null) {
//                                File tecFile = WorkWithFiles.getFileOnServer(pathToFile);
//                                if (tecFile != null) {
//                                    if (WorkWithFiles.transferFileOnServer(tecFile, fm.getNewName())) {
//                                        sendAnswerMessage(true, Consts.formatForDate.format(new Date()) + ". File " + fm.getName() + "  transferred!");
//                                    } else {
//                                        sendAnswerMessage(false, Consts.formatForDate.format(new Date()) + ". Error when transfer file " + fm.getName() + "...");
//                                    }
//                                } else {
//                                    sendAnswerMessage(false, Consts.formatForDate.format(new Date()) + ". File " + fm.getName() + " not found!");
//                                }
//
//                            } else {
//                                File file = WorkWithFiles.getFileOnServer(pathToFile);
//                                FileMessage outfm = new FileMessage(file.getName(), Files.readAllBytes(Paths.get(file.getAbsolutePath())), false, false, null, null);
//                                ObjectOutputStream oosSend = new ObjectOutputStream(client.getOutputStream());
//                                oosSend.writeObject(outfm);
//                                oosSend.flush();
//                            }
//                        } else {
//                            if (WorkWithFiles.saveFileOnDisk(pathToFile, fm)) {
//                                sendAnswerMessage(true, Consts.formatForDate.format(new Date()) + ". File " + fm.getName() + " written on disk");
//                            } else {
//                                sendAnswerMessage(false, Consts.formatForDate.format(new Date()) + ". Error when write file " + fm.getName() + "...");
//                            }
//                        }

                    }
                    if (obj instanceof FolderMessage) {
                        handleFolderMessage((FolderMessage) obj);

                    }
                    if (obj instanceof SecurityMessage) {
                        sendSecurityMessage();
                    }

                    if (obj instanceof LoginMessage) {
                        handleLoginMessage((LoginMessage) obj);

                    }
                    if (obj instanceof AnswerMessage) {
                        AnswerMessage ansmg = (AnswerMessage) obj;
                        if (ansmg.isYes()) {
                            sArea.append(Consts.formatForDate.format(new Date()) + ". " + ansmg.getMsg() + "\n");
//                            ois.close();
//                            if (client.isConnected()) client.close();
                            break;
                        }
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println(e.getLocalizedMessage());
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setTimeout(boolean timeout) {
        isTimeout = timeout;
    }

    public String getUser() {
        return user;
    }

    private String ChangePathFromLocalToServer(String localName) {
        StringBuilder sb = new StringBuilder(localName);
        if (sb.indexOf("\\") != -1) {
            sb.replace(0, sb.indexOf("\\"), bd.getUserUid());
        } else {
            sb.replace(0, sb.length(), bd.getUserUid());
        }
        return sb.toString();
    }

    private void sendSecurityMessage() {
        try {
            threadSecurity = new Security();
            threadKey = threadSecurity.getKey();

            ObjectOutputStream oos = null;
            try {
                oos = new ObjectOutputStream(client.getOutputStream());
                SecurityMessage sMsg = new SecurityMessage(threadKey);
                oos.writeObject(sMsg);
                oos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void handleTransferFileMessage(TransferFileMessage trFm) throws IOException, ClassNotFoundException {

        String PathOnServer = ChangePathFromLocalToServer(trFm.getName());
        String pathToFile = WorkWithFiles.DIR_PATH + PathOnServer;
        if (trFm.isTransfer() && trFm.isEndOfFile()) {
            if (WorkWithFiles.saveFileOnDisk(pathToFile, trFm.getData())) {
                Network.sendAnswerMessage(client, bd.getUserUid(), true, Consts.formatForDate.format(new Date()) + ". File " + trFm.getName() + " written on disk");
            } else {
                Network.sendAnswerMessage(client, bd.getUserUid(), false, Consts.formatForDate.format(new Date()) + ". Error when write file " + trFm.getName() + "...");
            }

        } else if (trFm.isTransfer() && !trFm.isEndOfFile()) {
            String fileName = trFm.getName();
            boolean getFile = Network.getFile(trFm, fileName, pathToFile, client);
//            File file = new File(pathToFile);
//            String fileName = trFm.getName();
//            long size = trFm.getSize();
//            ArrayList<byte[]> aList = new ArrayList<>();
//            aList.add(trFm.getData());
//            while (!trFm.isEndOfFile()) {
////                                sendAnswerMessage(true, null);
//                ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
//                trFm = (TransferFileMessage) ois.readObject();
//                aList.add(trFm.getData());
//
//            }
//
//            if (aList.size() > 0) {
//                FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
//                for (int i = 0; i < aList.size(); i++) {
//                    fos.write(aList.get(i));
//                    fos.flush();
//                }
//                fos.close();
//            }
//            aList.clear();

            if (getFile) {
                Network.sendAnswerMessage(client, bd.getUserUid(), true, Consts.formatForDate.format(new Date()) + ". File " + fileName + " written on disk");
            } else {
                Network.sendAnswerMessage(client, bd.getUserUid(), false, Consts.formatForDate.format(new Date()) + ". Error when write file " + fileName + "...");
            }

        } else if (!trFm.isTransfer()) {

        } else {
            File file = WorkWithFiles.getFileOnServer(pathToFile);
//            FileMessage outfm = new FileMessage(file.getName(), Files.readAllBytes(Paths.get(file.getAbsolutePath())), false, false, null, null);
            FileMessage outfm = new FileMessage(file.getName(), TypeFileActionEnum.GET, null, null);
            ObjectOutputStream oosSend = new ObjectOutputStream(client.getOutputStream());
            oosSend.writeObject(outfm);
            oosSend.flush();
        }
    }

    private void handleFileMessage(FileMessage fm) throws IOException, ClassNotFoundException {

        String PathOnServer = ChangePathFromLocalToServer(fm.getName());
        String pathToFile = WorkWithFiles.DIR_PATH + PathOnServer;
//        if (fm.getData() == null) {
        if (fm.getAction().equals(TypeFileActionEnum.DELETE)) {
            if (WorkWithFiles.deleteFileOnServer(pathToFile)) {
                Network.sendAnswerMessage(client, bd.getUserUid(), true, Consts.formatForDate.format(new Date()) + ". File " + fm.getName() + " deleted!");
            } else {
                Network.sendAnswerMessage(client, bd.getUserUid(), false, Consts.formatForDate.format(new Date()) + ". Error when delete file " + fm.getName() + "...");
            }
        } else if (fm.getAction().equals(TypeFileActionEnum.REFRESH)) {
        } else if (fm.getAction().equals(TypeFileActionEnum.GET)) {
            boolean getFile = Network.getFile(pathToFile, client, user);
//            if (getFile){
//                Network.sendAnswerMessage(client, user, true, "File: " + pathToFile + " saved on local disk.");
//            }else{
//                Network.sendAnswerMessage(client, user, false, "File: " + pathToFile + " not saved on local disk!");
//            }
        } else if (fm.getNewName() != null && fm.getTecPath() == null) {
            File tecFile = WorkWithFiles.getFileOnServer(pathToFile);
            if (tecFile != null) {
                if (WorkWithFiles.renameFileOnServer(tecFile, fm.getNewName())) {
                    Network.sendAnswerMessage(client, bd.getUserUid(), true, Consts.formatForDate.format(new Date()) + ". File " + fm.getName() + "  renamed!");
                } else {
                    Network.sendAnswerMessage(client, bd.getUserUid(), false, Consts.formatForDate.format(new Date()) + ". Error when renamed file " + fm.getName() + "...");
                }

            } else {
                Network.sendAnswerMessage(client, bd.getUserUid(), false, Consts.formatForDate.format(new Date()) + ". File " + fm.getName() + " not found!");
            }

        } else if (fm.getNewName() != null && fm.getTecPath() != null) {
            File tecFile = WorkWithFiles.getFileOnServer(pathToFile);
            String newServerName = ChangePathFromLocalToServer(fm.getNewName());
            if (tecFile != null) {
                if (WorkWithFiles.transferFileOnServer(tecFile, newServerName)) {
                    Network.sendAnswerMessage(client, bd.getUserUid(), true, Consts.formatForDate.format(new Date()) + ". File " + fm.getName() + "  transferred!");
                } else {
                    Network.sendAnswerMessage(client, bd.getUserUid(), false, Consts.formatForDate.format(new Date()) + ". Error when transfer file " + fm.getName() + "...");
                }
            } else {
                Network.sendAnswerMessage(client, bd.getUserUid(), false, Consts.formatForDate.format(new Date()) + ". File " + fm.getName() + " not found!");
            }
        }
//        } else {
//            if (WorkWithFiles.saveFileOnDisk(pathToFile, fm)) {
//                sendAnswerMessage(true, Consts.formatForDate.format(new Date()) + ". File " + fm.getName() + " written on disk");
//            } else {
//                sendAnswerMessage(false, Consts.formatForDate.format(new Date()) + ". Error when write file " + fm.getName() + "...");
//            }
//        }
    }

    private void handleLoginMessage(LoginMessage lm) {

        String str = threadSecurity.decrypt(lm.getStrongName());
        String[] userInfo = str.split(";");
        if (userInfo.length != 2) {
            Network.sendAnswerMessage(client, null, false, Consts.formatForDate.format(new Date()) + ". Invalid user data format! Connection refused...");
            return;
        }
        user = userInfo[0];
        if (lm.isNewUser()) {
            if (bd.addUser(this, userInfo[0], userInfo[1])) {
                Network.sendAnswerMessage(client, bd.getUserUid(), true, Consts.formatForDate.format(new Date()) + ". User " + user + " added.");
                sArea.append(Consts.formatForDate.format(new Date()) + ". User " + user + " connected. \n");
                clientsTime.put(client, System.currentTimeMillis());
            } else {
                Network.sendAnswerMessage(client, null, false, Consts.formatForDate.format(new Date()) + ". Error add new user: " + user);
            }
        } else {
            if (bd.getUser(this, userInfo[0], userInfo[1])) {
                Network.sendAnswerMessage(client, bd.getUserUid(), true, Consts.formatForDate.format(new Date()) + ". User " + user + " connected.");
                sArea.append(Consts.formatForDate.format(new Date()) + ". User " + user + " connected. \n");
                clientsTime.put(client, System.currentTimeMillis());
            } else {
                Network.sendAnswerMessage(client, null, false, Consts.formatForDate.format(new Date()) + ". User " + user + " not found! Register new user.");
            }
        }
    }

    private void handleFolderMessage(FolderMessage fdm) {

        String PathOnServer = ChangePathFromLocalToServer(fdm.getName());
//        String pathToDir = WorkWithFiles.DIR_PATH + fdm.getName();
        String pathToDir = WorkWithFiles.DIR_PATH + PathOnServer;
        if (fdm.isCreate()) {
//            if (WorkWithFiles.makeDir(fdm.getName() + "\\" + fdm.getNewName())) {
            if (WorkWithFiles.makeDir(PathOnServer + "\\" + fdm.getNewName())) {
                Network.sendAnswerMessage(client, bd.getUserUid(), true, Consts.formatForDate.format(new Date()) + ". Folder " + fdm.getNewName() + "  created!");
            } else {
                Network.sendAnswerMessage(client, bd.getUserUid(), false, Consts.formatForDate.format(new Date()) + ". Error when create folder " + fdm.getName() + "...");
            }
        } else if (fdm.isDelete()) {
            if (WorkWithFiles.deleteDirOnServer(pathToDir)) {
                Network.sendAnswerMessage(client, bd.getUserUid(), true, Consts.formatForDate.format(new Date()) + ". Folder " + fdm.getName() + "  deleted!");
            } else {
                Network.sendAnswerMessage(client, bd.getUserUid(), false, Consts.formatForDate.format(new Date()) + ". Error when delete folder " + fdm.getName() + "...");
            }
        } else {
            File tecFolder = WorkWithFiles.getFolderOnServer(pathToDir);
            if (tecFolder != null) {
                if (WorkWithFiles.renameFileOnServer(tecFolder, fdm.getNewName())) {
                    Network.sendAnswerMessage(client, bd.getUserUid(), true, Consts.formatForDate.format(new Date()) + ". Folder " + fdm.getName() + "  renamed!");
                } else {
                    Network.sendAnswerMessage(client, bd.getUserUid(), false, Consts.formatForDate.format(new Date()) + ". Error when renamed folder " + fdm.getName() + "...");
                }
            } else {
                Network.sendAnswerMessage(client, bd.getUserUid(), false, Consts.formatForDate.format(new Date()) + ". Folder " + fdm.getName() + " not found!");
            }
        }
    }

}
