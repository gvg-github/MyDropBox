import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadServer extends Thread {

    private static MultiThreadServer instance = null;
    private PasswordAuthentication pa;
    private BD bd;
    private int port;
    private JTextArea textArea;
    private ServerSocket serverSocket;
    private ServerThread sThread;
    private ArrayList<ServerThread> threadList;

    private static ExecutorService ex = Executors.newFixedThreadPool(2);

    public static synchronized MultiThreadServer getInstance(JTextArea textArea, int port){
        if(instance == null){
            instance = new MultiThreadServer(textArea, port);
        }
        return instance;
    }

    private MultiThreadServer(JTextArea textArea, int port) {
        this.port = port;
        this.textArea = textArea;
        threadList = new ArrayList<>();
        bd = new BD();
        pa = new PasswordAuthentication();
    }

    @Override
    public void run() {

        try {
            InetAddress address = InetAddress.getLocalHost();//172.16.172.252

//            serverSocket = new ServerSocket(port, 2, address);
            serverSocket = new ServerSocket(port, 2);

            if (serverSocket == null) {
                textArea.append(Consts.formatForDate.format(new Date()) + ". Not find free port! Server not started!\n");
                interrupt();
            }

            textArea.append(Consts.formatForDate.format(new Date()) + ". Server started. IP: " + address + ", port: " + serverSocket.getLocalPort() + "\n");
            while (!serverSocket.isClosed()) {
                Socket client =  serverSocket.accept();
                sThread = new ServerThread(this, client, textArea, bd, pa);
                ex.execute(sThread);
                threadList.add(sThread);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopCurrentServer() {

        try {
            serverSocket.close();
            textArea.append(Consts.formatForDate.format(new Date()) + ". Closing - DONE.\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<ServerThread> getThreadList() {
        return threadList;
    }
}

