import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class MessageSender extends Thread {


    private File file;
    private String filename;
    private Socket clientSocket;
    private AnswerMessage aMsg;

    public MessageSender(File file, String filename, Socket clientSocket){
        this.file = file;
        this.filename = filename;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            aMsg = Network.sendFile(file, filename, clientSocket);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public AnswerMessage getaMsg() {
        return aMsg;
    }
}
