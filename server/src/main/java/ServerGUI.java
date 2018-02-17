import javafx.scene.input.KeyCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

class ServerGUI extends JFrame implements ActionListener {

    private JTextArea textArea;
    private MultiThreadServer server;
    private JButton startButton;
    private JButton stopButton;

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
        startButton = new JButton("Start server");
        startButton.setMnemonic(java.awt.event.KeyEvent.VK_R);
        startButton.addActionListener(this);
        stopButton = new JButton("Stop server");
        stopButton.setMnemonic(java.awt.event.KeyEvent.VK_S);
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
        if (e.getSource() == startButton) {
            if (server == null || !server.isAlive()){
                server = new MultiThreadServer(textArea, Consts.PORT);
                server.start();
            }
        } else if (e.getSource() == stopButton) {
            if (server != null && server.isAlive()) server.stopCurrentServer();
        }
    }


}
