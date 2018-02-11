import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadServer extends Thread {

    private BD bd;
    private int port;
    private JTextArea textArea;
    private ServerSocket serverSocket;
    private ServerThread sThread;
//    private Thread timeController;
    private ArrayList<ServerThread> threadList;
    private HashMap<Socket, Long> clientsTime = new HashMap<>();

    private static ExecutorService ex = Executors.newFixedThreadPool(2);

    public MultiThreadServer(JTextArea textArea, int port) {
        this.port = port;
        this.textArea = textArea;
        threadList = new ArrayList<>();
        bd = new BD();
    }

    @Override
    public void run() {

//        startTimeoutController();


        try {
            InetAddress address = InetAddress.getLocalHost();//172.16.172.252

            serverSocket = new ServerSocket(port, 2, address);

            if (serverSocket == null) {
                textArea.append(Consts.formatForDate.format(new Date()) + ". Not find free port! Server not started!\n");
                interrupt();
            }

            textArea.append(Consts.formatForDate.format(new Date()) + ". Server started. IP: " + address + ", port: " + serverSocket.getLocalPort() + "\n");
            while (!serverSocket.isClosed()) {
                Socket client =  serverSocket.accept();
                sThread = new ServerThread(this, client, textArea, port, bd, clientsTime);
                ex.execute(sThread);
//                client.setThread(sThread);

                threadList.add(sThread);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    private void startTimeoutController() {
//                timeController = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    while(true){
//
//                        sleep(10000);
//                        System.out.println("Controller running");
//                        for (Map.Entry entry : clientsTime.entrySet()
//                                ) {
//                            long clientTime = (Long) entry.getValue();
//                            Socket selectedClient = (Socket) entry.getKey();
//                            if (System.currentTimeMillis() - clientTime > 120000) {
//                                selectedClient.getThread().setTimeout(true);
////                                System.out.println("Клиент: " + selectedClient + ", системное время: " + System.nanoTime() + ", время подключения: " + clientTime);
//                                textArea.append(Consts.formatForDate.format(new Date()) + ". Client: " + selectedClient.getThread().getUser() + " - timeout!");
//                                System.out.println("Клиент: " + selectedClient + ", системное время: " + (System.currentTimeMillis() - clientTime));
////                                Network.sendAnswerMessage(selectedClient, null, true, Consts.formatForDate.format(new Date()) + ". Client disconnected for timeout!");
//                            }
//                        }
//                    }
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
////        timeController.setDaemon(true);
//        timeController.start();
//    }

    public void stopCurrentServer() {

        try {
            serverSocket.close();
//            timeController.interrupt();
            textArea.append(Consts.formatForDate.format(new Date()) + ". Closing - DONE.\n");
        } catch (IOException e) {
//            textArea.append(e.getLocalizedMessage() + "\n");
            e.printStackTrace();
        }
    }

    public ArrayList<ServerThread> getThreadList() {
        return threadList;
    }
}

