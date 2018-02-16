
import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientGUI_WindowAdapter implements WindowListener {

    private ClientGUI myFrame;

    public ClientGUI_WindowAdapter(ClientGUI myFrame){
        this.myFrame = myFrame;
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent event) {
        try {
            Socket tecSocket = myFrame.getClientSocket();
            if (tecSocket != null && !tecSocket.isClosed()) {
                String msg = "User: " + myFrame.getLogin() + " disconnected";
                AnswerMessage aMsg = new AnswerMessage(true, msg, null, 0);
                ObjectOutputStream oos = new ObjectOutputStream(tecSocket.getOutputStream());
                oos.writeObject(aMsg);
                oos.flush();
                oos.close();
                if (!tecSocket.isClosed()) {
                    tecSocket.close();
                }
                tecSocket = null;
            }
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
