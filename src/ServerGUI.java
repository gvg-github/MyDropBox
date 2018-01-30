import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerGUI implements ActionListener {

    public static final String DBPATH_DISK = "D:";
    public static final String DBPATH_DIR = "FilesDB";
    public static final String DIR_PATH = "D:\\FilesDB";
    private JFrame frame;
    private JTextArea textArea;
    private ServerThread sThread;
    private MultiThreadServer server;

    public static void main(String[] args) {
        new ServerGUI();
    }

    private ServerGUI() {

        frame = new JFrame("Server window");
        frame.setSize(300, 300);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel buttonPanel = new JPanel();
        JButton startButton = new JButton("Start server");
        startButton.addActionListener(this);
        JButton stopButton = new JButton("Stop server");
        stopButton.addActionListener(this);

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        textArea = new JTextArea();

        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.add(textArea, BorderLayout.CENTER);
        frame.setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Start server")) {

            server = new MultiThreadServer(textArea, 8089);
            server.start();


        } else if (e.getActionCommand().equals("Stop server")) {
            if (server != null && server.isAlive()) server.stopCurrentServer();

        }
    }
}
