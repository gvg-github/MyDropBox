
import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.text.ParseException;
import java.util.Date;

import static java.lang.Thread.sleep;

public class ClientStartFrame extends JFrame {

    private Socket clientSocket;
    private SecretKey clientKey;
    private ClientGUI client;

    private JTextField ipAdress = new JTextField("localhost", 12);
    private JTextField port;
    private JButton autoConnectButton;
    private JButton connectButton;
    private JButton exitButton;
    private JCheckBox settings;
    private JTextArea textArea;
    boolean breakConnect = false;

    public ClientStartFrame(ClientGUI client) {


        this.client = client;
        setTitle("Connect to server");
//        setSize(600, 100);
        setResizable(false);
        setLocation(500, 100);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new MyDispatcher());

        Font otherFont = new Font("TimesRoman", Font.BOLD, 16);

        try {
            port = new JFormattedTextField(new DefaultFormatterFactory(new MaskFormatter("####")), Consts.PORT);
        } catch (ParseException e) {
            port = new JTextField(Integer.toString(Consts.PORT));
        }

        ipAdress.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (ipAdress.getText().length() >= 15)
                    e.consume();
            }
        });

        port.setFont(otherFont);
        ipAdress.setFont(otherFont);
        readWriteSystemFile(true);

        settings = new JCheckBox();

        textArea = new JTextArea(10, 20);//"Greetings! If you're here by accident, push \"Exit\", else try another buttons. :-)", 5, 20);
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setFont(Font.getFont(Font.DIALOG));

        JPanel fieldsPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel ipText = new JLabel("Server IP:");
        JLabel portText = new JLabel("port:");
        ipText.setFont(otherFont);
        portText.setFont(otherFont);
        fieldsPanel1.add(ipText);
        fieldsPanel1.add(ipAdress);
        fieldsPanel1.add(portText);
        fieldsPanel1.add(port);

        connectButton = new JButton("Connect");
        connectButton.addActionListener(new StartActionListener());

        autoConnectButton = new JButton("Auto connect");
        autoConnectButton.addActionListener(new StartActionListener());
        autoConnectButton.setToolTipText("For break press \"Ctrl\"");

        exitButton = new JButton("Exit");
        exitButton.addActionListener(new StartActionListener());

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        buttonsPanel.setBorder(BorderFactory.createBevelBorder(1));
        buttonsPanel.add(connectButton);
        buttonsPanel.add(autoConnectButton);
        buttonsPanel.add(exitButton);

        JPanel fieldsPanel3 = new JPanel(new BorderLayout());
        fieldsPanel3.add(fieldsPanel1, BorderLayout.NORTH);
        fieldsPanel3.add(buttonsPanel, BorderLayout.SOUTH);
        fieldsPanel3.setBorder(BorderFactory.createBevelBorder(2));

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBorder(BorderFactory.createBevelBorder(1));
        textPanel.add(new JScrollPane(textArea));

        add(fieldsPanel3, BorderLayout.CENTER);
        add(textPanel, BorderLayout.SOUTH);

        pack();
        setVisible(true);
    }

    private void readWriteSystemFile(boolean read) {
        File loadFile = new File("System");
        StringBuilder stb = new StringBuilder();
        if (read){
            if (loadFile.exists()){
                try {
                    FileReader fr= new FileReader(loadFile);
                    int c = 0;
                    while ((c = fr.read()) != -1){
                        stb.append((char)c);
                    }
                    String[] fileData = stb.toString().split(";");
                    if (fileData.length > 0){
                        ipAdress.setText(fileData[0]);
                        port.setText(fileData[1]);
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else{
            try {
                stb.append(ipAdress.getText() + ";" + port.getText());
                FileWriter fwr = new FileWriter(loadFile, false);
                fwr.write(stb.toString());
                fwr.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeStartFrame() {
        setVisible(false);
        readWriteSystemFile(false);
        client.start();
        dispose();
    }

    class StartActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == exitButton) {
                System.exit(0);
            } else if (e.getSource() == connectButton) {
                if (connectToSocket()) {
                    client.setClientKey(clientKey);
                    client.setClientSocket(clientSocket);
                    closeStartFrame();
                }

            } else if (e.getSource() == autoConnectButton) {

                Thread connectThread = new Thread(() -> {
                    boolean connected = false;
                    while (!connected && !breakConnect) {
                        connected = connectToSocket();
                        if (connected) break;
                        try {
                            sleep(2000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }

                    if (breakConnect) {
                        textArea.append("Auto connection stopped by user. \n");
                        breakConnect = false;
                    }
                    if (clientSocket != null) {
                        client.setClientKey(clientKey);
                        client.setClientSocket(clientSocket);
                        closeStartFrame();
                    }
                });
                connectThread.start();
            }
        }

        private boolean connectToSocket() {
            try {
                clientSocket = new Socket(ipAdress.getText(), Integer.parseInt(port.getText()));
            } catch (IOException e1) {
                textArea.append(Consts.formatForDate.format(new Date()) + ". Can't connect to server... \n");
            }
            if (clientSocket != null) {
                try {
                    clientSocket.setSoTimeout(30000);
                    if (connectAccepted(clientSocket)) {
                        if (sendSecurityMessage()) {
                            clientSocket.setSoTimeout(0);
                            return true;
                        } else {
                            textArea.append(Consts.formatForDate.format(new Date()) + "Security system not work! Try again later... \n");
                        }
                    } else {
                        clientSocket = null;
                    }
                } catch (SocketException e1) {
                    clientSocket = null;
                    textArea.append(Consts.formatForDate.format(new Date()) + ". Server is busy! Try again later... \n");
                }
            }
            return false;
        }

        private boolean sendSecurityMessage() {

            SecurityMessage sMsg = new SecurityMessage();
            ObjectOutputStream oos = null;
            try {
                oos = new ObjectOutputStream(clientSocket.getOutputStream());
                oos.writeObject(sMsg);
                oos.flush();
                ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                SecurityMessage asMsg = (SecurityMessage) ois.readObject();
                clientKey = asMsg.getKey();
                if (clientKey != null) return true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return false;
        }

        private boolean connectAccepted(Socket clientSocket) {

            try {
                ObjectInputStream ois = null;
                ois = new ObjectInputStream(clientSocket.getInputStream());
                Object obj = ois.readObject();
                if (obj instanceof AnswerMessage) {
                    AnswerMessage ansmg = (AnswerMessage) obj;
                    textArea.append(ansmg.getMsg() + "\n");
                    return true;
                }
            } catch (IOException e) {
                textArea.append(Consts.formatForDate.format(new Date()) + ". Server is busy! Try again later... \n");
            } catch (ClassNotFoundException e) {
                textArea.append(Consts.formatForDate.format(new Date()) + ". Server is busy! Try again later... \n");
            }
            return false;
        }
    }

    private class MyDispatcher implements KeyEventDispatcher {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
//            if (e.getKeyCode() == 38 && e.isControlDown()) {
            if (e.isControlDown()) {
                breakConnect = true;
            }
            return false;
        }
    }

}
