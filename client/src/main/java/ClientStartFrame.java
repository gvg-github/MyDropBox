
import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;

import static java.lang.Thread.sleep;

public class ClientStartFrame extends JFrame {

    private Socket clientSocket;
    private SecretKey clientKey;
    private File[] userFiles;

    private JTextField ipAdress = new JTextField("localhost", 8);
    private JTextField port;
    private JButton exitButton;
    private JButton registerButton;
    private JButton loginButton;
    private JButton changePassButton;
    private JButton getFileButton;
    private JTextField idField;
    private JCheckBox autoConnect;

    private JTextField login = new JTextField(12);
    private JPasswordField password = new JPasswordField(12);
    private String newPass;

    private JTextArea textArea;
    boolean breakConnect = false;
    private int userSize;
    private boolean connected;


    public ClientStartFrame() {

        setTitle("Connect to server");
        setResizable(false);
        setLocation(500, 250);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new MyDispatcher());

        Font otherFont = new Font("TimesRoman", Font.BOLD, 16);

        login.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (login.getText().length() >= 20)
                    e.consume();
            }
        });

        password.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (password.getPassword().length >= 20)
                    e.consume();
            }
        });

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

        readWriteSystemFile(true);

        textArea = new JTextArea(10, 20);
        textArea.setEditable(false);
        textArea.setFont(Font.getFont(Font.DIALOG));

        JPanel fieldsPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel ipText = new JLabel("Server IP:");
        JLabel portText = new JLabel("port:");

        fieldsPanel1.add(ipText);
        fieldsPanel1.add(ipAdress);
        fieldsPanel1.add(portText);
        fieldsPanel1.add(port);
        JLabel loginText = new JLabel("Login:");
        fieldsPanel1.add(loginText);
        fieldsPanel1.add(login);
        JLabel passText = new JLabel("Password:");
        fieldsPanel1.add(passText);
        fieldsPanel1.add(password);

        autoConnect = new JCheckBox();
        autoConnect.setFont(otherFont);
        autoConnect.setToolTipText("For break press \"Ctrl\" + \"Q\"");
        JPanel loginTextPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField addIdTextFiled = new JTextField("Autoconnect");
        addIdTextFiled.setEditable(false);
        addIdTextFiled.setFont(otherFont);
        addIdTextFiled.setToolTipText("For break press \"Ctrl\" + \"Q\"");
        loginTextPanel.add(autoConnect);
        loginTextPanel.add(addIdTextFiled);

        exitButton = new JButton("Exit");
        exitButton.addActionListener(new StartActionListener());
        loginButton = new JButton("Login");
        loginButton.addActionListener(new StartActionListener());
        registerButton = new JButton("Register");
        registerButton.addActionListener(new StartActionListener());

        changePassButton = new JButton("Change password");
        changePassButton.addActionListener(new StartActionListener());

        getFileButton = new JButton("Get file from ID");
        getFileButton.addActionListener(new StartActionListener());
        idField = new JTextField("Enter file ID here...", 18);

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        buttonsPanel.setBorder(BorderFactory.createBevelBorder(1));
        buttonsPanel.add(loginButton);
        buttonsPanel.add(changePassButton);
        buttonsPanel.add(registerButton);
        buttonsPanel.add(exitButton);

        JPanel logPassPanel = new JPanel(new BorderLayout());
        logPassPanel.add(buttonsPanel, BorderLayout.SOUTH);
        logPassPanel.add(loginTextPanel, BorderLayout.NORTH);

        JPanel getFilePanel = new JPanel(new BorderLayout());
        getFilePanel.add(getFileButton, BorderLayout.WEST);
        getFilePanel.add(idField, BorderLayout.CENTER);
        getFilePanel.setBorder(BorderFactory.createBevelBorder(1));

        JPanel fieldsPanel3 = new JPanel(new BorderLayout());
        fieldsPanel3.add(fieldsPanel1, BorderLayout.NORTH);
        fieldsPanel3.add(logPassPanel, BorderLayout.CENTER);
        fieldsPanel3.add(getFilePanel, BorderLayout.SOUTH);
        fieldsPanel3.setBorder(BorderFactory.createBevelBorder(2));

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBorder(BorderFactory.createBevelBorder(1));
        textPanel.add(new JScrollPane(textArea));

        add(fieldsPanel3, BorderLayout.CENTER);
        add(textPanel, BorderLayout.SOUTH);

        pack();
        setVisible(true);
    }

    public ClientStartFrame(String msg) {

        ClientStartFrame startClient = new ClientStartFrame();
        startClient.textArea.append(msg + "\n");

    }

    private void readWriteSystemFile(boolean read) {
        File loadFile = new File("System");
        StringBuilder stb = new StringBuilder();
        if (read) {
            if (loadFile.exists()) {
                try {
                    FileReader fr = new FileReader(loadFile);
                    int c = 0;
                    while ((c = fr.read()) != -1) {
                        stb.append((char) c);
                    }
                    String[] fileData = stb.toString().split(";");
                    if (fileData.length > 0) {
                        ipAdress.setText(fileData[0]);
                        port.setText(fileData[1]);
                        if (fileData.length == 3) {
                            login.setText(fileData[2]);
                        }
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {

            try {
                if (!loadFile.exists()) {
                    try {
                        loadFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                stb.append(ipAdress.getText() + ";" + port.getText() + ";" + login.getText());
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
        new ClientGUI(clientSocket, userFiles, userSize, login.getText());
        dispose();
    }

    public void setNewPass(String newPass) {
        this.newPass = newPass;
    }

    class StartActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == exitButton) {
                System.exit(0);
            }
            lockButtons();
            if (e.getSource() == loginButton || e.getSource() == registerButton || e.getSource() == changePassButton) {
                if (login.getText().equals("") || password.getPassword().length == 0) {
                    JOptionPane.showMessageDialog(null, "Please fill fields \"Login\" and \"Password\"!", "", JOptionPane.WARNING_MESSAGE);
                    unlockButtons();
                    return;
                }

                if (autoConnect.isSelected()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            textArea.append(Consts.formatForDate.format(new Date()) + ". Connecting... \n");
                            while (true) {
                                try {
                                    clientSocket = new Socket(ipAdress.getText(), Integer.parseInt(port.getText()));
                                    if (clientSocket != null && clientSocket.isConnected()) {
                                        connected = connectToSocket();
                                    }
                                    if (connected) break;
                                    if (breakConnect) {
                                        textArea.append(Consts.formatForDate.format(new Date()) + ". Interrupted by user. \n");
                                        breakConnect = false;
                                        break;
                                    }
                                    clientSocket = null;
                                    Thread.sleep(1000);

                                } catch (IOException e) {
                                    textArea.append(Consts.formatForDate.format(new Date()) + ". Can't connect to server... \n");
                                    textArea.append(e.getMessage() + "\n");
                                } catch (InterruptedException e) {
                                    textArea.append(e.getMessage() + "\n");
                                }
                            }
                            if (clientSocket != null) connectionEstablished(clientSocket, e.getSource());
                        }
                    }).start();
                } else {
                    if (tryConnect()) {
                        connectionEstablished(clientSocket, e.getSource());
                    }
                }
            } else if (e.getSource() == getFileButton) {
                if (idField.getText().equals("") || idField.getText().equals("Enter file ID here...")) {
                    JOptionPane.showMessageDialog(null, "Please enter file ID!", "", JOptionPane.WARNING_MESSAGE);
                    unlockButtons();
                    return;
                }
                if (tryConnect()) {
                    connectionEstablished(clientSocket, e.getSource());

                }
            }
            unlockButtons();
        }

        private void unlockButtons() {
            getFileButton.setEnabled(true);
            registerButton.setEnabled(true);
            loginButton.setEnabled(true);
            changePassButton.setEnabled(true);
        }

        private void lockButtons() {
            getFileButton.setEnabled(false);
            registerButton.setEnabled(false);
            loginButton.setEnabled(false);
            changePassButton.setEnabled(false);
        }

        private void getFileOnServerFromID() {

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int ret = fileChooser.showDialog(null, "Select file");
            if (ret == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    String fileID = idField.getText();
                    FileMessage fm = new FileMessage(null, FileActionEnum.GET, null, fileID);
                    ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                    oos.writeObject(fm);
                    oos.flush();
                    ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                    Object obj = null;
                    try {
                        obj = ois.readObject();
                        if (obj instanceof TransferFileMessage) {
                            TransferFileMessage trMsg = (TransferFileMessage) obj;
                            String localPathToFile = file.getAbsolutePath() + "\\" + trMsg.getName();
                            boolean getFile = Network.getFile(trMsg, trMsg.getName(), localPathToFile, clientSocket);
                            if (getFile) {
                                Network.sendAnswerMessage(clientSocket, null, true, null);
                                textArea.append(Consts.formatForDate.format(new Date()) + ". File saved to: " + localPathToFile + ".\n");
                            } else {
                                Network.sendAnswerMessage(clientSocket, null, false, null);
                                textArea.append(Consts.formatForDate.format(new Date()) + ". File not saved to: " + localPathToFile + "!\n");
                            }

                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        private void connectionEstablished(Socket clientSocket, Object source) {

            if (source == loginButton) {
                if (sendLoginMessage(UserActionEnum.GET)) {
                    closeStartFrame();
                }
            } else if (source == registerButton) {
                if (sendLoginMessage(UserActionEnum.ADD)) {
                    closeStartFrame();
                }
            } else if (source == changePassButton) {
                setEnabled(false);
                new NewPassFrame(this);

            } else if (source == getFileButton) {
                getFileOnServerFromID();
                try {
                    clientSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                clientSocket = null;
            }
        }

        private boolean tryConnect() {

            try {
                clientSocket = new Socket(ipAdress.getText(), Integer.parseInt(port.getText()));

            } catch (IOException e1) {
                textArea.append(Consts.formatForDate.format(new Date()) + ". Can't connect to server... \n");
            }

            if (clientSocket != null) {
                connected = connectToSocket();
                if (connected) return true;
            }
            return false;
        }

        private boolean connectToSocket() {

            try {
//                clientSocket.setSoTimeout(30000);
                clientSocket.setSoTimeout(10000);
                if (connectAccepted(clientSocket)) {
                    if (sendSecurityMessage()) {
                        clientSocket.setSoTimeout(0);
                        return true;
                    } else {
                        textArea.append(Consts.formatForDate.format(new Date()) + "MyDropBoxSecurity system not work! Try again later... \n");
                    }
                } else {
                    clientSocket.close();
                    clientSocket = null;
                }
            } catch (SocketException e1) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    textArea.append(Consts.formatForDate.format(new Date()) + ". IO error... \n");
                }
                textArea.append(Consts.formatForDate.format(new Date()) + ". Socket error... \n");
            } catch (IOException e) {
                textArea.append(Consts.formatForDate.format(new Date()) + ". IO error... \n");
            }

            clientSocket = null;
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

        private boolean sendLoginMessage(UserActionEnum userAction) {

            boolean logIn = false;
            if (clientKey != null) {
                StringBuilder sb = new StringBuilder();
                if (userAction.equals(UserActionEnum.CHANGE)) {

                    if (newPass != null) {
                        sb.append(login.getText() + ";" + Arrays.toString(password.getPassword()) + ";" + newPass);
                    } else {
                        JOptionPane.showMessageDialog(null, "New pass is empty! Try again...");
                        return false;
                    }
                } else {
                    sb.append(login.getText() + ";" + Arrays.toString(password.getPassword()));
                }
                String loginStr = MyDropBoxSecurity.encrypt(sb.toString(), clientKey);
                if (loginStr != null) {
                    LoginMessage lm = new LoginMessage(loginStr, userAction);
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                        oos.writeObject(lm);
                        oos.flush();
                        ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                        AnswerMessage aMsg = (AnswerMessage) ois.readObject();
                        textArea.append(Consts.formatForDate.format(new Date()) + ". " + aMsg.getMsg() + "\n");
                        JOptionPane.showMessageDialog(null, aMsg.getMsg(), "", JOptionPane.WARNING_MESSAGE);
                        if (aMsg.isYes()) {
                            userFiles = aMsg.getFiles();
                            userSize = aMsg.getSize();
                            logIn = true;
                        }

                    } catch (IOException e) {
                        logIn = false;
                        e.printStackTrace();
                        textArea.append(Consts.formatForDate.format(new Date()) + ". " + e.getLocalizedMessage() + "\n");
                    } catch (ClassNotFoundException e) {
                        logIn = false;
                        e.printStackTrace();
                    }
                } else {
                    logIn = false;
                    textArea.append(Consts.formatForDate.format(new Date()) + ". MyDropBoxSecurity error! Connection is not allowed...\n");
                }
            } else {
                logIn = false;
                textArea.append(Consts.formatForDate.format(new Date()) + ". MyDropBoxSecurity key not found! Connection is not allowed...\n");
            }
            if (!logIn) {
                try {
                    clientSocket.close();
                    clientSocket = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return logIn;

        }

        private boolean connectAccepted(Socket clientSocket) {

            try {
                ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
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

            if (e.getKeyCode() == KeyEvent.VK_Q && e.isControlDown()) {
                breakConnect = true;
                return true;
            }
            return false;
        }
    }

    private class NewPassFrame extends JFrame {

//        private StartActionListener listener;

        public NewPassFrame(StartActionListener listener) {

//            this.listener = listener;
            setSize(300, 100);
            setTitle("Enter new password");
            setResizable(false);
            int[] coords = ClientStartFrame.getStartCoords(this);
            setLocation((int) (coords[0]- getWidth()) / 2, (int) (coords[1] - getHeight()) / 2);

            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            JPasswordField passwordNew = new JPasswordField(12);
//            passwordNew.setFont(new Font("TimesRoman", Font.BOLD, 16));
            JButton setPass = new JButton("OK");
            setPass.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setNewPass(Arrays.toString(passwordNew.getPassword()));
                    dispose();
                    if (newPass != null) {
                        if (!newPass.equals(Arrays.toString(password.getPassword()))) {
                            if (listener.sendLoginMessage(UserActionEnum.CHANGE)) {
                                closeStartFrame();
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "Pass not changed!", "", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            });
            add(passwordNew, BorderLayout.CENTER);
            add(setPass, BorderLayout.SOUTH);
            pack();
            setVisible(true);
        }
    }

    public static int[] getStartCoords(JFrame frame){
        int[] coords = new int[2];
        Toolkit kit = frame.getToolkit();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        Insets in = kit.getScreenInsets(gs[0].getDefaultConfiguration());
        Dimension d = kit.getScreenSize();
        coords[0] = (d.width - in.left - in.right);
        coords[1] = (d.height - in.top - in.bottom);
        return coords;
    }

}

