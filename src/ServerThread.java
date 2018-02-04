
import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ServerThread extends Thread {

    private MultiThreadServer mtSever;
    private int port;
    private JTextArea sArea;

    private Socket client;
    private String user;
    private BD bd;
    private SimpleDateFormat formatForDate = new SimpleDateFormat("hh:mm:ss a");

    public ServerThread(MultiThreadServer mtSever, Socket client, JTextArea sArea, int port) {
        this.port = port;
        this.sArea = sArea;
        this.client = client;
        this.mtSever = mtSever;
    }

    @Override
    public void run() {

        try {
            while (!client.isClosed()) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                    Object obj = ois.readObject();
                    String msg = null;
                    if (obj instanceof FileMessage) {
                        FileMessage fm = (FileMessage) obj;
                        String pathToFile = Network.DIR_PATH + fm.getName();
                        if (fm.getData() == null) {
                            if (fm.isDelete()) {
                                if (Network.deleteFileOnServer(pathToFile)) {
                                    sendAnswerMessage(true, formatForDate.format(new Date()) + ". File " + fm.getName() + " deleted!");
                                } else {
                                    sendAnswerMessage(false, formatForDate.format(new Date()) + ". Error when delete file " + fm.getName() + "...");
                                }
                            } else if (fm.isRefresh()) {
                            } else if (fm.getNewName() != null && fm.getTecPath() == null) {
                                File tecFile = Network.getFileOnServer(pathToFile);
                                if (tecFile != null) {
                                    if (Network.renameFileOnServer(tecFile, fm.getNewName())) {
                                        sendAnswerMessage(true, formatForDate.format(new Date()) + ". File " + fm.getName() + "  renamed!");
                                    } else {
                                        sendAnswerMessage(false, formatForDate.format(new Date()) + ". Error when renamed file " + fm.getName() + "...");
                                    }

                                } else {
                                    sendAnswerMessage(false, formatForDate.format(new Date()) + ". File " + fm.getName() + " not found!");
                                }

                            } else if (fm.getNewName() != null && fm.getTecPath() != null) {
                                File tecFile = Network.getFileOnServer(pathToFile);
                                if (tecFile != null) {
                                    if (Network.transferFileOnServer(tecFile, fm.getNewName())) {
                                        sendAnswerMessage(true, formatForDate.format(new Date()) + ". File " + fm.getName() + "  transferred!");
                                    } else {
                                        sendAnswerMessage(false, formatForDate.format(new Date()) + ". Error when transfer file " + fm.getName() + "...");
                                    }
                                } else {
                                    sendAnswerMessage(false, formatForDate.format(new Date()) + ". File " + fm.getName() + " not found!");
                                }

                            } else {
                                File file = Network.getFileOnServer(pathToFile);
                                FileMessage outfm = new FileMessage(file.getName(), Files.readAllBytes(Paths.get(file.getAbsolutePath())), false, false, null, null);
                                ObjectOutputStream oosSend = new ObjectOutputStream(client.getOutputStream());
                                oosSend.writeObject(outfm);
                                oosSend.flush();
                            }
                        } else {
                            if (Network.saveFileOnDisk(pathToFile, fm)) {
                                sendAnswerMessage(true, formatForDate.format(new Date()) + ". File " + fm.getName() + " written on disk");
                            } else {
                                sendAnswerMessage(false, formatForDate.format(new Date()) + ". Error when write file " + fm.getName() + "...");
                            }
                        }

                    }
                    if (obj instanceof FolderMessage) {
                        FolderMessage fdm = (FolderMessage) obj;
                        String pathToDir = Network.DIR_PATH + fdm.getName();
                        if (fdm.isCreate()) {
                            if (Network.makeDir(fdm.getName() + "\\" + fdm.getNewName())) {
                                sendAnswerMessage(true, formatForDate.format(new Date()) + ". Folder " + fdm.getNewName() + "  created!");
                            } else {
                                sendAnswerMessage(false, formatForDate.format(new Date()) + ". Error when create folder " + fdm.getName() + "...");
                            }
                        } else if (fdm.isDelete()) {
                            if (Network.deleteDirOnServer(pathToDir)) {
                                sendAnswerMessage(true, formatForDate.format(new Date()) + ". Folder " + fdm.getName() + "  deleted!");
                            } else {
                                sendAnswerMessage(false, formatForDate.format(new Date()) + ". Error when delete folder " + fdm.getName() + "...");
                            }
                        } else {
                            File tecFolder = Network.getFolderOnServer(pathToDir);
                            if (tecFolder != null) {
//                                if (Network.renameFolderOnServer(tecFolder, fdm.getNewName())) {
                                if (Network.renameFileOnServer(tecFolder, fdm.getNewName())) {
                                    sendAnswerMessage(true, formatForDate.format(new Date()) + ". Folder " + fdm.getName() + "  renamed!");
                                } else {
                                    sendAnswerMessage(false, formatForDate.format(new Date()) + ". Error when renamed folder " + fdm.getName() + "...");
                                }
                            } else {
                                sendAnswerMessage(false, formatForDate.format(new Date()) + ". Folder " + fdm.getName() + " not found!");
                            }
                        }
                    }
                    if (obj instanceof LoginMessage) {

                        LoginMessage lm = (LoginMessage) obj;
                        bd = new BD();
                        if (lm.getNewUser()) {
                            if (bd.addUser(this, lm.getName(), lm.getPass())) {
                                user = lm.getName();
                                sendAnswerMessage(true, formatForDate.format(new Date()) + ". User " + lm.getName() + " added.");

                            } else {
                                sendAnswerMessage(false, formatForDate.format(new Date()) + ". Error add new user: " + lm.getName());
                            }
                        } else {
                            if (bd.getUser(this, lm.getName(), lm.getPass())) {
                                user = lm.getName();
                                sendAnswerMessage(true, formatForDate.format(new Date()) + ". User " + lm.getName() + " connected.");
                            } else {
                                sendAnswerMessage(false, formatForDate.format(new Date()) + ". User " + lm.getName() + " not found! Register new user.");
                            }
                        }
                    }
                    if (obj instanceof AnswerMessage) {
                        AnswerMessage ansmg = (AnswerMessage) obj;
                        if (ansmg.isYes()) {
                            sArea.append(formatForDate.format(new Date()) + ". " + ansmg.getMsg() + "\n");
                            ois.close();
                            if (client.isConnected()) client.close();
                            break;
                        }
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println(e.getLocalizedMessage());
                }

            }
            this.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendAnswerMessage(boolean b, String s) {
        File[] userFiles = Network.getUserFileStructure(user);
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(client.getOutputStream());
            AnswerMessage aMsg = new AnswerMessage(b, s, userFiles);
            oos.writeObject(aMsg);
            oos.flush();
//            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void interrupt() {
        if (bd != null) {
            bd.disconnect(this);
        }
        super.interrupt();
        ArrayList<ServerThread> threadList = mtSever.getThreadList();
        if (!threadList.isEmpty() && threadList.contains(this)) threadList.remove(this);
        sArea.append(formatForDate.format(new Date()) + ". Thread " + this.getName() + " stopped." + "\n");
    }
}
