
import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ServerThread extends Thread {

    private int port;
    private JTextArea sArea;

    private Socket client;
    private String user;
    private BD bd;

    public ServerThread(Socket client, JTextArea sArea, int port) {
        this.port = port;
        this.sArea = sArea;
        this.client = client;
    }

    @Override
    public void run() {

        try {

            while (!client.isClosed()) {
                ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                try {
                    Object obj = ois.readObject();
                    String msg = null;
                    if (obj instanceof FileMessage) {
                        FileMessage fm = (FileMessage) obj;
                        if (Network.saveFileOnDisk(user, fm)) {
                            msg = "File " + fm.getName() + " written on disk";
                        } else {
                            msg = "Error when write file " + fm.getName() + "...";
                        }
                    }
                    if (obj instanceof LoginMessage) {

                        LoginMessage lm = (LoginMessage) obj;
                        bd = new BD();
                        if (lm.getNewUser()) {
                            if (bd.addUser(this, lm.getName(), lm.getPass())) {
                                user = lm.getName();
                                msg = "User " + lm.getName() + " added.";
                            } else {
                                msg = "Error add new user: " + lm.getName();
                            }
                        } else {
                            if (bd.getUser(this, lm.getName(), lm.getPass())) {
                                user = lm.getName();
                                msg = "User " + lm.getName() + " connected.";
                            } else {
                                msg = "User " + lm.getName() + " not found! Register new user.";
                            }
                        }
                    }
                    ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                    oos.writeUTF(msg);
                    oos.flush();

                } catch (ClassNotFoundException e) {
                    System.out.println(e.getLocalizedMessage());
                }

            }
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
        sArea.append("Thread stopped." + "\n");
    }
}
