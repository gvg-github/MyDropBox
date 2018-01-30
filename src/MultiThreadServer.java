import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MultiThreadServer extends Thread{

    private int port;
    private JTextArea textArea;
    private ServerSocket serverSocket;
    private ServerThread sThread;
    private ArrayList<ServerThread> threadList;

    public MultiThreadServer(JTextArea textArea, int port) {
        this.port = port;
        this.textArea = textArea;
        threadList = new ArrayList<>();
    }


    @Override
    public void run() {
//        super.run();
        try {
            serverSocket = new ServerSocket(port);
            while (!serverSocket.isClosed()){
                textArea.append("Server socket ready." + "\n");
                Socket client = serverSocket.accept();
                sThread = new ServerThread(client, textArea, port);
                sThread.start();
                textArea.append("Create new thread: " + sThread.getName() + "\n");
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
            textArea.append("Closing - DONE.\n");
        } catch (IOException e) {
            textArea.append(e.getLocalizedMessage() + "\n");
            e.printStackTrace();
        }
    }
}

