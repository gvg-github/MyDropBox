
import javax.crypto.SecretKey;
import javax.swing.*;
import java.io.*;
import java.net.Socket;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ServerThread implements Runnable {

    private MultiThreadServer mtSever;
    private JTextArea sArea;
    private PasswordAuthentication pa;

    private Socket client;
    private String user;
    private BD bd;
    private SecretKey threadKey;
    private MyDropBoxSecurity threadMyDropBoxSecurity;

    public ServerThread(MultiThreadServer mtSever, Socket client, JTextArea sArea, BD bd, PasswordAuthentication pa) {
        this.sArea = sArea;
        this.client = client;
        this.mtSever = mtSever;
        this.bd = bd;
        this.pa = pa;
    }

    @Override
    public void run() {

        Network.sendAnswerMessage(client, null, true, Consts.formatForDate.format(new Date()) + ". Socket ready!");

        while (!client.isClosed()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                Object obj = ois.readObject();
                if (obj instanceof TransferFileMessage) {
                    handleTransferFileMessage((TransferFileMessage) obj);
                }
                if (obj instanceof FileMessage) {
                    handleFileMessage((FileMessage) obj);

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
                        break;
                    }
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                break;

            } catch (IOException e) {
                e.printStackTrace();
                break;

            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        mtSever.getThreadList().remove(this);
    }

    public String getUser() {
        return user;
    }

    public PasswordAuthentication getPa() {
        return pa;
    }

    private String ChangePathFromLocalToServer(String localName) {
        StringBuilder sb = new StringBuilder(localName);
        if (sb.indexOf("\\") != -1) {
            sb.replace(0, sb.indexOf("\\"), bd.getUserUID());
        } else {
            sb.replace(0, sb.length(), bd.getUserUID());
        }
        return sb.toString();
    }

    private void sendSecurityMessage() {
        try {
            threadMyDropBoxSecurity = new MyDropBoxSecurity();
            threadKey = threadMyDropBoxSecurity.getKey();

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

    private void handleTransferFileMessage(TransferFileMessage trFm) throws IOException, ClassNotFoundException, SQLException {

        String PathOnServer = ChangePathFromLocalToServer(trFm.getName());
        String pathToFile = Consts.DIR_PATH + PathOnServer;
        if (trFm.isTransfer() && trFm.isEndOfFile()) {
            if (WorkWithFiles.saveFileOnDisk(pathToFile, trFm.getData())) {
                String fileName = new File(pathToFile).getName();
                if (bd.addFile(this, fileName, pathToFile, trFm.getPath(), trFm.getSize(), System.currentTimeMillis())) {
                    Network.sendAnswerMessage(client, bd.getUserUID(), true, Consts.formatForDate.format(new Date()) + ". File " + trFm.getName() + " is written on disk");
                } else {
                    Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.formatForDate.format(new Date()) + ". Error writing file data " + trFm.getName() + " to BD...");
                }
            } else {
                Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.formatForDate.format(new Date()) + ". Error writing file " + trFm.getName() + "...");
            }

        } else if (trFm.isTransfer() && !trFm.isEndOfFile()) {
            String fileName = trFm.getName();
            boolean getFile = Network.getFile(trFm, fileName, pathToFile, client);

            if (getFile) {
                fileName = new File(pathToFile).getName();
                if (bd.addFile(this, fileName, pathToFile, trFm.getPath(), trFm.getSize(), System.currentTimeMillis())) {
                    Network.sendAnswerMessage(client, bd.getUserUID(), true, Consts.formatForDate.format(new Date()) + ". File " + trFm.getName() + " is written on disk");
                } else {
                    Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.formatForDate.format(new Date()) + ". Error writing file data " + trFm.getName() + " to BD...");
                }
            } else {
                Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.formatForDate.format(new Date()) + ". Error when write file " + fileName + "...");
            }

//        } else if (!trFm.isTransfer()) {
//
//        } else {
//            File file = WorkWithFiles.getFileOnServer(pathToFile);
//            FileMessage outfm = new FileMessage(file.getName(), FileActionEnum.GET, null, null);
//            ObjectOutputStream oosSend = new ObjectOutputStream(client.getOutputStream());
//            oosSend.writeObject(outfm);
//            oosSend.flush();
        }
    }

    private void handleFileMessage(FileMessage fm) throws IOException, ClassNotFoundException, SQLException {

        //+ get file from ID
        if (fm.getName() == null && fm.getAction().equals(FileActionEnum.GET)) {
            String pathToFile = bd.getFilePathOnServer(this, fm.getTecPath());
            Network.getFile(pathToFile, client);
            return;
        } // - get file from ID

        String PathOnServer = ChangePathFromLocalToServer(fm.getName());
        String pathToFile = Consts.DIR_PATH + PathOnServer;

        if (fm.getAction().equals(FileActionEnum.DELETE)) {
            if (WorkWithFiles.deleteFileOnServer(pathToFile)) {
                if (bd.deleteFile(this, pathToFile)) {
                    Network.sendAnswerMessage(client, bd.getUserUID(), true, Consts.formatForDate.format(new Date()) + ". File " + fm.getName() + " deleted!");
                } else {
                    Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.formatForDate.format(new Date()) + ". Error deleting file data " + fm.getName() + " from BD...");
                }
            } else {
                Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.formatForDate.format(new Date()) + ". Error when delete file " + fm.getName() + "...");
            }

        } else if (fm.getAction().equals(FileActionEnum.REFRESH)) {
            String localPath = bd.getFileLocalPath(this, pathToFile);
            if (WorkWithFiles.deleteFileOnServer(pathToFile)) {
                if (bd.deleteFile(this, pathToFile)) {
                    Network.sendAnswerMessage(client, null, true, localPath);
                } else {
                    Network.sendAnswerMessage(client, null, false, Consts.formatForDate.format(new Date()) + ". Error deleting file data " + fm.getName() + " from BD...");
                }
            } else {
                Network.sendAnswerMessage(client, null, false, Consts.formatForDate.format(new Date()) + ". Error when delete file " + pathToFile + "...");
            }

        } else if (fm.getAction().equals(FileActionEnum.GET)) {
            Network.getFile(pathToFile, client);

        } else if (fm.getAction().equals(FileActionEnum.GET_ID)) {
            String fileID = bd.getFileID(this, pathToFile);
            if (fileID != null) {
                Network.sendAnswerMessage(client, null, true, fileID);
            } else {
                Network.sendAnswerMessage(client, null, false, "Write about file: " + pathToFile + " not found!");
            }

        } else if (fm.getAction().equals(FileActionEnum.RENAME)) {
            File tecFile = WorkWithFiles.getFileOnServer(pathToFile);
            if (tecFile != null) {
                String newPath = WorkWithFiles.createNewPathForFile(tecFile, fm.getNewName());
                if (WorkWithFiles.verifyPathForFile(newPath)) {
                    String tecPath = tecFile.getAbsolutePath();
                    if (WorkWithFiles.renameFileOnServer(tecFile, newPath)) {
                        boolean renamedInBD = bd.renameFile(this, tecPath, newPath);
                        if (renamedInBD) {
                            Network.sendAnswerMessage(client, bd.getUserUID(), true, Consts.formatForDate.format(new Date()) + ". File " + fm.getName() + "  renamed!");
                        } else {
                            Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.formatForDate.format(new Date()) + ". Error when write file data " + fm.getName() + " in BD...");
                        }
                    } else {
                        Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.formatForDate.format(new Date()) + ". Error when renamed file " + fm.getName() + "...");
                    }
                } else {
                    Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.formatForDate.format(new Date()) + ". File with name " + fm.getName() + " already exists! Try another name!");
                }

            } else {
                Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.formatForDate.format(new Date()) + ". File " + fm.getName() + " not found!");
            }

        } else if (fm.getAction().equals(FileActionEnum.TRANSFER)) {
            File tecFile = WorkWithFiles.getFileOnServer(pathToFile);
            String newServerName = ChangePathFromLocalToServer(fm.getNewName());
            if (tecFile != null) {
                String fileName = tecFile.getName();
                String newPath = Consts.DIR_PATH + newServerName + "\\" + fileName;
                if (WorkWithFiles.transferFileOnServer(tecFile, newServerName)) {
                    if (bd.transferFile(this, pathToFile, newPath)) {
                        Network.sendAnswerMessage(client, bd.getUserUID(), true, Consts.formatForDate.format(new Date()) + ". File " + fm.getName() + "  transferred!");
                    } else {
                        Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.formatForDate.format(new Date()) + ". Error when rewrite file data " + fm.getName() + " in BD...");
                    }
                } else {
                    Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.formatForDate.format(new Date()) + ". Error when transfer file " + fm.getName() + "...");
                }
            } else {
                Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.formatForDate.format(new Date()) + ". File " + fm.getName() + " not found!");
            }
        }
    }

    private void handleLoginMessage(LoginMessage lm) throws Exception {

        String str = threadMyDropBoxSecurity.decrypt(lm.getStrongName());
        String[] userInfo = str.split(";");
        if (lm.getUserActionEnum().equals(UserActionEnum.CHANGE)) {
            if (userInfo.length != 3) {
                Network.sendAnswerMessage(client, null, false, Consts.formatForDate.format(new Date()) + ". Invalid user data format! Connection refused...");
                return;
            }
        } else {
            if (userInfo.length != 2) {
                Network.sendAnswerMessage(client, null, false, Consts.formatForDate.format(new Date()) + ". Invalid user data format! Connection refused...");
                return;
            }
        }
        user = userInfo[0];
        if (lm.getUserActionEnum().equals(UserActionEnum.ADD)) {
            if (bd.getUser(this, userInfo[0])) {
                Network.sendAnswerMessage(client, null, false, Consts.formatForDate.format(new Date()) + ". User already exist! Try login.");
            }
            if (bd.addUser(this, userInfo[0], userInfo[1])) {
                Network.sendAnswerMessage(client, bd.getUserUID(), true, Consts.formatForDate.format(new Date()) + ". User " + user + " added.");
                sArea.append(Consts.formatForDate.format(new Date()) + ". User " + user + " connected. \n");

            } else {
                Network.sendAnswerMessage(client, null, false, Consts.formatForDate.format(new Date()) + ". Error add new user: " + user);
            }

        } else if (lm.getUserActionEnum().equals(UserActionEnum.GET)) {
            ArrayList<ServerThread> threadList = mtSever.getThreadList();

            //+ verify user already connected.
            for (int i = 0; i < threadList.size(); i++) {
                if (threadList.get(i).equals(this)) continue;
                if (threadList.get(i).getUser().equals(user)) {
                    Network.sendAnswerMessage(client, null, false, Consts.formatForDate.format(new Date()) + ". User " + user + " already connected! Connection refused.");
                    return;
                }
            } //- verify user already connected.

            if (bd.getUser(this, userInfo[0])) {
                if (bd.verifyUser(this, userInfo[0], userInfo[1])) {
                    Network.sendAnswerMessage(client, bd.getUserUID(), true, Consts.formatForDate.format(new Date()) + ". User " + user + " connected.");
                    sArea.append(Consts.formatForDate.format(new Date()) + ". User " + user + " connected. \n");
                } else {
                    Network.sendAnswerMessage(client, null, false, Consts.formatForDate.format(new Date()) + ". Wrong login or password! Connection refused.");
                }
            } else {
                Network.sendAnswerMessage(client, null, false, Consts.formatForDate.format(new Date()) + ". User " + user + " not found! Register new user.");
            }

        } else {
            if (bd.getUser(this, userInfo[0])) {
                if (bd.verifyUser(this, userInfo[0], userInfo[1])) {
                    if (bd.changePassword(this, userInfo[0], userInfo[2])) {
                        sArea.append(Consts.formatForDate.format(new Date()) + ". User " + user + " connected. \n");
                        Network.sendAnswerMessage(client, bd.getUserUID(), true, Consts.formatForDate.format(new Date()) + ". User " + user + " connected.");
                    } else {
                        Network.sendAnswerMessage(client, null, false, Consts.formatForDate.format(new Date()) + ". Error change password! Connection refused.");
                    }
                } else {
                    Network.sendAnswerMessage(client, null, false, Consts.formatForDate.format(new Date()) + ". Wrong login or password! Connection refused.");
                }
            } else {
                Network.sendAnswerMessage(client, null, false, Consts.formatForDate.format(new Date()) + ". User " + user + " not found! Register new user.");
            }
        }
    }

    private void handleFolderMessage(FolderMessage fdm) {

        String PathOnServer = ChangePathFromLocalToServer(fdm.getName());
        String pathToDir = Consts.DIR_PATH + PathOnServer;
        if (fdm.isCreate()) {
            if (WorkWithFiles.makeDir(PathOnServer + "\\" + fdm.getNewName())) {
                Network.sendAnswerMessage(client, bd.getUserUID(), true, Consts.formatForDate.format(new Date()) + ". Folder " + fdm.getNewName() + "  created!");
            } else {
                Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.formatForDate.format(new Date()) + ". Error when create folder " + fdm.getName() + "...");
            }
        } else if (fdm.isDelete()) {
            if (WorkWithFiles.deleteDirOnServer(pathToDir)) {
                Network.sendAnswerMessage(client, bd.getUserUID(), true, Consts.formatForDate.format(new Date()) + ". Folder " + fdm.getName() + "  deleted!");
            } else {
                Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.formatForDate.format(new Date()) + ". Error when delete folder " + fdm.getName() + "...");
            }
        } else {
            File tecFolder = WorkWithFiles.getFolderOnServer(pathToDir);
            if (tecFolder != null) {
                String newPath = WorkWithFiles.createNewPathForFile(tecFolder, fdm.getNewName());
                if (WorkWithFiles.verifyPathForFile(newPath)) {
                    if (WorkWithFiles.renameFolderOnServer(tecFolder, newPath)) {
                        Network.sendAnswerMessage(client, bd.getUserUID(), true, Consts.formatForDate.format(new Date()) + ". Folder " + fdm.getName() + "  renamed!");
                    } else {
                        Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.formatForDate.format(new Date()) + ". Error when renamed folder " + fdm.getName() + "...");
                    }
                } else {
                    Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.formatForDate.format(new Date()) + ". Folder with name " + fdm.getName() + " already exists! Try another name!");
                }
            } else {
                Network.sendAnswerMessage(client, bd.getUserUID(), false, Consts.formatForDate.format(new Date()) + ". Folder " + fdm.getName() + " not found!");
            }
        }
    }
}
