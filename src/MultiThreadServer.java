import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MultiThreadServer extends Thread{

    private int port;
    private JTextArea textArea;
    private ServerSocket serverSocket;
    private ServerThread sThread;
    private ArrayList<ServerThread> threadList;
    private SimpleDateFormat formatForDate = new SimpleDateFormat("hh:mm:ss a");

    public MultiThreadServer(JTextArea textArea, int port) {
        this.port = port;
        this.textArea = textArea;
        threadList = new ArrayList<>();
    }

    @Override
    public void run() {
//        super.run();
        try {
            InetAddress address = InetAddress.getLocalHost();//172.16.172.252
//            InetAddress address = InetAddress.getByName("localhost");
//            serverSocket = new ServerSocket(port, 2, address);
            serverSocket = new ServerSocket(port, 2);
            textArea.append(formatForDate.format(new Date()) + ". Server started. IP: " + address + "\n");
            while (!serverSocket.isClosed()){
                textArea.append("Server socket ready." + "\n");
                Socket client = serverSocket.accept();
                sThread = new ServerThread(this, client, textArea, port);
                sThread.start();
                textArea.append(formatForDate.format(new Date()) + ". Create new thread: " + sThread.getName() + " " + client.getInetAddress() + "\n");
                threadList.add(sThread);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopCurrentServer(){
        if (!threadList.isEmpty()){
            for (ServerThread thr: threadList
                    ) {
                thr.interrupt();
            }
        }
        try {
            serverSocket.close();
            textArea.append(formatForDate.format(new Date()) + ". Closing - DONE.\n");
        } catch (IOException e) {
//            textArea.append(e.getLocalizedMessage() + "\n");
            e.printStackTrace();
        }
    }

    public ArrayList<ServerThread> getThreadList() {
        return threadList;
    }
}

