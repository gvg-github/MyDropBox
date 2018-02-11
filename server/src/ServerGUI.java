import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class ServerGUI extends JFrame implements ActionListener {

    private JTextArea textArea;
    private MultiThreadServer server;

    public static void main(String[] args) {
        new ServerGUI();
    }

    private ServerGUI() {
        start();
    }

    private void start(){

        setTitle("Server window");
        setSize(390, 200);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel buttonPanel = new JPanel();
        JButton startButton = new JButton("Start server");
        startButton.addActionListener(this);
        JButton stopButton = new JButton("Stop server");
        stopButton.addActionListener(this);

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        textArea = new JTextArea();
        textArea.setEditable(false);

        getContentPane().add(buttonPanel, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Start server")) {

            server = new MultiThreadServer(textArea, Consts.PORT);
            server.start();


        } else if (e.getActionCommand().equals("Stop server")) {
            if (server != null && server.isAlive()) server.stopCurrentServer();

        }
    }
}
