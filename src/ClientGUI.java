import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;

import java.net.SocketException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;


public class ClientGUI extends JFrame implements ActionListener {

    private JFrame clientFrame;
    private JTextField login = new JTextField("Login", 10);
    private JTextField password = new JPasswordField("Password", 10);

    private JButton registerButton;
    private JButton connectButton;

    private JButton getFromIdButton;
    private JTextField inputFileId;

    private JButton sendFileButton;
    private JButton getFileButton;
    private JButton deleteFileButton;
    private JButton refreshFileButton;
    private JButton renameFileButton;
    private JButton transferFileButton;
    private JButton createDirButton;
    private JButton renameDirButton;
    private JButton deleteDirButton;
    private JPanel buttonsPanel;

    //Должен иметься функционал по отправке файлов на сервер, скачиванию, удалению, обновлению, переименование,
    // перемещение файлов в рамках хранилища. Общение клиента с сервером организуется через команды.

    private File file;
    private JTextArea textArea;
    private JPanel centerPanel;
    private JPanel contents;
    private JTree tree1;

    private StringBuilder selectedPath;
    DefaultMutableTreeNode tecNode;
    ArrayList<String> folderList = new ArrayList<>();

    private SecretKey clientKey;
    private Socket clientSocket;
    private boolean LogIn;

    public static void main(String[] args) {
        ClientGUI newClient = new ClientGUI();
        new ClientStartFrame(newClient);
    }

    private ClientGUI() {

    }

    public void start() {

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
                if (password.getText().length() >= 20)
                    e.consume();
            }
        });

        selectedPath = new StringBuilder();

        clientFrame = new JFrame("Client window");
        clientFrame.setSize(600, 600);
        clientFrame.setMinimumSize(new Dimension(300, 300));
        clientFrame.setLocation(500, 100);
        clientFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        clientFrame.addWindowListener(new ClientGUI_WindowAdapter(this));

        Font otherFont = new Font("TimesRoman", Font.BOLD, 15);

        //Верхняя панель , логин, регистрация нового пользователя, получение файла по ID без логина
        JPanel loginPanel = new JPanel(new BorderLayout());

        connectButton = new JButton("Connect");
        connectButton.setFont(otherFont);
        connectButton.addActionListener(this);
        registerButton = new JButton("Register");
        registerButton.setFont(otherFont);
        registerButton.addActionListener(this);
        getFromIdButton = new JButton("    Get file from ID   ");
        getFromIdButton.setFont(otherFont);
        getFromIdButton.addActionListener(this);
        inputFileId = new JTextField("Enter file ID here...", 35);

        JPanel loginButtonsPanel = new JPanel(new GridLayout(3,0, 5, 5));
        loginButtonsPanel.add(connectButton);
        loginButtonsPanel.add(registerButton);
        loginButtonsPanel.add(getFromIdButton);
        loginButtonsPanel.setBorder(BorderFactory.createBevelBorder(1));

        JPanel loginTextPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel loginText = new JLabel("Login:");
        loginText.setFont(otherFont);
        loginTextPanel1.add(loginText);
        login.setFont(otherFont);
        loginTextPanel1.add(login);
        JLabel passText = new JLabel("Password:");
        passText.setFont(otherFont);
        loginTextPanel1.add(passText);
        password.setFont(otherFont);
        loginTextPanel1.add(password);

        JPanel loginTextPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputFileId.setFont(otherFont);
        loginTextPanel2.add(inputFileId);

        JPanel loginTextPanel3 = new JPanel(new BorderLayout());
        loginTextPanel3.add(loginTextPanel1, BorderLayout.NORTH);
        loginTextPanel3.add(loginTextPanel2, BorderLayout.SOUTH);

        JPanel loginTextPanel4 = new JPanel(new GridLayout(1, 0));
        loginTextPanel4.add(loginTextPanel3);
        loginPanel.setBorder(BorderFactory.createBevelBorder(0));
        loginPanel.add(loginTextPanel4, BorderLayout.CENTER);
        loginPanel.add(loginButtonsPanel, BorderLayout.EAST);

        //Правая панель с кнопками для работы с файлами и папками
        sendFileButton = new JButton("Send file to storage");
        sendFileButton.addActionListener(this);
        getFileButton = new JButton("Get selected file");
        getFileButton.addActionListener(this);
        deleteFileButton = new JButton("Delete selected file");
        deleteFileButton.addActionListener(this);
        refreshFileButton = new JButton("Refresh selected file");
        refreshFileButton.addActionListener(this);
        renameFileButton = new JButton("Rename selected file");
        renameFileButton.addActionListener(this);
        transferFileButton = new JButton("Transfer selected file");
        transferFileButton.addActionListener(this);
        createDirButton = new JButton("Create folder");
        createDirButton.addActionListener(this);
        renameDirButton = new JButton("Rename selected folder");
        renameDirButton.addActionListener(this);
        deleteDirButton = new JButton("Delete selected folder");
        deleteDirButton.addActionListener(this);

        buttonsPanel = new JPanel(new GridLayout(12, 0, 5, 5));
        JLabel fileActions = new JLabel("Actions with files:");
//        fileActions.setHorizontalTextPosition(SwingConstants.TRAILING);
        fileActions.setBorder(BorderFactory.createBevelBorder(0));
        buttonsPanel.add(fileActions);
        buttonsPanel.add(sendFileButton);
        buttonsPanel.add(getFileButton);
        buttonsPanel.add(renameFileButton);
        buttonsPanel.add(refreshFileButton);
        buttonsPanel.add(transferFileButton);
        buttonsPanel.add(deleteFileButton);
        JLabel folderActions = new JLabel("Actions with folders:");
//        fileActions.setHorizontalTextPosition(SwingConstants.LEADING);
        folderActions.setBorder(BorderFactory.createBevelBorder(0));
        buttonsPanel.add(folderActions);
        buttonsPanel.add(createDirButton);
        buttonsPanel.add(renameDirButton);
        buttonsPanel.add(deleteDirButton);
        buttonsPanel.setBorder(BorderFactory.createBevelBorder(1));

        textArea = new JTextArea(5, 20);

        centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        centerPanel.setBorder(BorderFactory.createBevelBorder(1));

        //Дерево файлов пользователя
        contents = new JPanel(new GridLayout(1, 0));
        tree1 = new JTree(createTreeModel("User files will be here...", null));
        contents.add(new JScrollPane(tree1));
        contents.setBorder(BorderFactory.createBevelBorder(1));

        //Вывод на фрейм
        clientFrame.add(loginPanel, BorderLayout.NORTH);
        clientFrame.add(centerPanel, BorderLayout.SOUTH);
        clientFrame.add(contents, BorderLayout.CENTER);
        clientFrame.add(buttonsPanel, BorderLayout.EAST);

        clientFrame.pack();
        clientFrame.setMinimumSize(clientFrame.getSize());
        clientFrame.setVisible(true);
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void setClientKey(SecretKey clientKey) {
        this.clientKey = clientKey;
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (clientSocket != null && !clientSocket.isClosed()) {
            if (LogIn){
                if (e.getSource() == getFileButton) {
                    if (selectedPath.length() == 0 || tecNode == null || !tecNode.isLeaf()) {
                        JOptionPane.showMessageDialog(null, "File not selected!\n", "", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    sendFileMessage(TypeFileActionEnum.GET, null, null);
                } else if (e.getSource() == deleteFileButton) {
                    if (selectedPath.length() == 0 || tecNode == null || !tecNode.isLeaf()) {
                        JOptionPane.showMessageDialog(null, "File not selected!\n", "", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    int res = JOptionPane.showConfirmDialog(this, "Really delete this file?");
                    if (res == JOptionPane.NO_OPTION) return;
                    sendFileMessage(TypeFileActionEnum.DELETE, null, null);

                } else if (e.getSource() == renameFileButton) {
                    if (selectedPath.length() == 0 || tecNode == null || !tecNode.isLeaf()) {
                        JOptionPane.showMessageDialog(null, "File not selected!\n", "", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    String newFileName = JOptionPane.showInputDialog("Input file name:");
                    if (newFileName != null) {
                        sendFileMessage(TypeFileActionEnum.RENAME, newFileName, null);
                    }
                } else if (e.getSource() == transferFileButton) {
                    if (selectedPath.length() == 0 || tecNode == null || !tecNode.isLeaf()) {
                        JOptionPane.showMessageDialog(null, "File not selected!\n", "", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    StringBuilder tecFolder = selectedPath;
                    if (tecNode.isLeaf()) {
                        tecFolder = getFolder(selectedPath.toString());
                    }
                    String tecPath = selectedPath.toString();


                    Object res = JOptionPane.showInputDialog(this, "Select new folder for file:", "", JOptionPane.QUESTION_MESSAGE, null, (Object[]) folderList.toArray(), folderList.get(0));
                    if (res == null) {
                        JOptionPane.showMessageDialog(null, "New folder not selected!\n", "", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    String newFolder = (String) res;
                    String newPath = newFolder;

                    if (newPath.equals(tecPath) || newFolder.equals(tecFolder.toString())) {
                        JOptionPane.showMessageDialog(null, "New folder not selected!\n", "", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    sendFileMessage(TypeFileActionEnum.TRANSFER, newPath, tecPath);
                } else if (e.getSource() == refreshFileButton) {
//                if (selectedPath.length() == 0 || tecNode == null || !tecNode.isLeaf()){
//                    JOptionPane.showMessageDialog(null, "File not selected!\n", "", JOptionPane.WARNING_MESSAGE);
//                    return;
//                }
                } else if (e.getSource() == createDirButton) {
                    sendFolderMessage(true, false);

                } else if (e.getSource() == renameDirButton) {
                    sendFolderMessage(false, false);

                } else if (e.getSource() == deleteDirButton) {
                    sendFolderMessage(false, true);

                } else if (e.getSource() == sendFileButton) {
                    sendFileMessage(TypeFileActionEnum.SEND,null, null);
                }

            }else{
                if (e.getSource() == connectButton) {
                    sendLoginMessage(false);
//                if (sendSecurityMessage()) {
//                    sendLoginMessage(false);
//                } else {
//                    textArea.append("Security system not work! Try again later... \n");
//                }
                } else if (e.getSource() == registerButton) {
                    sendLoginMessage(true);
//                if (sendSecurityMessage()) {
//                    sendLoginMessage(true);
//                } else {
//                    textArea.append("Security system not work! Try again later... \n");
                }
            }


        }
    }

//    private boolean sendSecurityMessage() {
//
//        SecurityMessage sMsg = new SecurityMessage();
//        ObjectOutputStream oos = null;
//        try {
//            oos = new ObjectOutputStream(clientSocket.getOutputStream());
//            oos.writeObject(sMsg);
//            oos.flush();
//            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
//            SecurityMessage asMsg = (SecurityMessage) ois.readObject();
//            clientKey = asMsg.getKey();
//            if (clientKey != null) return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        return false;
//
//    }

    private void sendFolderMessage(boolean create, boolean delete) {
        AnswerMessage ansMsg = null;
        String folderName = null;
        if (create) {
            String newFolderName = JOptionPane.showInputDialog("Input folder name:");
            if (newFolderName != null) {
                folderName = getPathToFolder(true, false);
                ansMsg = Network.sendFolderMessage(clientSocket, folderName, true, false, newFolderName);
            }
        } else if (delete) {
            if (selectedPath.length() == 0 || tecNode == null || !tecNode.getAllowsChildren()) {
                JOptionPane.showMessageDialog(null, "Folder not selected!\n", "", JOptionPane.WARNING_MESSAGE);
                return;
            }
            folderName = getPathToFolder(false, true);
            ansMsg = Network.sendFolderMessage(clientSocket, folderName, false, true, null);

        } else {
            if (selectedPath.length() == 0 || tecNode == null || !tecNode.getAllowsChildren()) {
                JOptionPane.showMessageDialog(null, "Folder not selected!\n", "", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String newFolderName = JOptionPane.showInputDialog("Input new folder name:");
            if (newFolderName != null) {
                folderName = selectedPath.toString() + "\\";
                ansMsg = Network.sendFolderMessage(clientSocket, folderName, false, false, newFolderName);
            }
        }

        if (ansMsg != null) {
            if (ansMsg.isYes()) {
                if (ansMsg.getFiles() != null) refreshTree(ansMsg.getFiles());
            }
            textArea.append(ansMsg.getMsg() + "\n");
//                        ois.close();
//                    oos.close();
        }
    }

    private String ChangePathFromServerToLocal(String localName) {
        StringBuilder sb = new StringBuilder(localName);
        sb.replace(0, sb.indexOf("\\"), login.getText());
        return sb.toString();
    }

    private String getPathToFolder(boolean create, boolean delete) {
        String folderName = null;
        if (create) {
            folderName = login.getText() + "\\";//  + newFolderName;
            if (selectedPath.length() != 0 && tecNode.getAllowsChildren()) {// && WorkWithFiles.verifyPath(selectedPath.toString())) {
                folderName = selectedPath.toString() + "\\";// + newFolderName;
            } else if (tecNode != null && !tecNode.getAllowsChildren()) {
                int index = selectedPath.lastIndexOf(tecNode.toString());
                StringBuilder newSelectedPath = selectedPath.delete(index, selectedPath.length());
                folderName = newSelectedPath.toString() + "\\";// + newFolderName;
            }
        } else if (delete) {
            folderName = login.getText();//  + newFolderName;
            if (selectedPath.length() != 0 && tecNode.getAllowsChildren()) {// && WorkWithFiles.verifyPath(selectedPath.toString())) {
                folderName = selectedPath.toString();// + newFolderName;
            } else if (tecNode != null && !tecNode.getAllowsChildren()) {
                int index = selectedPath.lastIndexOf(tecNode.toString());
                StringBuilder newSelectedPath = selectedPath.delete(index, selectedPath.length());
                folderName = newSelectedPath.toString();// + newFolderName;
            }
        }
        return folderName;
    }

    private StringBuilder getFolder(String tecName) {
        StringBuilder nameBuilder = new StringBuilder(selectedPath);

        int index1 = tecName.lastIndexOf("\\");
        nameBuilder.delete(index1, nameBuilder.length());
        return nameBuilder;
    }

    private void sendFileMessage(TypeFileActionEnum type, String newName, String tecPath) {
        if (type.equals(TypeFileActionEnum.SEND)) {
            AnswerMessage ansMsg = null;
            JFileChooser fileChooser = new JFileChooser();
            int ret = fileChooser.showDialog(null, "Select file");
            if (ret == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                String filename = login.getText() + "\\" + file.getName();
                if (selectedPath.length() != 0 && tecNode.getAllowsChildren()) {// && WorkWithFiles.verifyPath(selectedPath.toString())) {
                    filename = selectedPath.toString() + "\\" + file.getName();
                } else if (tecNode != null && !tecNode.getAllowsChildren()) {
                    int index = selectedPath.lastIndexOf(tecNode.toString());
                    StringBuilder newSelectedPath = selectedPath.delete(index, selectedPath.length());
                    filename = newSelectedPath.toString() + "\\" + file.getName();
                }

                try {
                    ansMsg = Network.sendFile(file, filename, clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if (ansMsg != null) {
                    if (ansMsg.isYes()) {
                        if (ansMsg.getFiles() != null) refreshTree(ansMsg.getFiles());
                    }
                    textArea.append(Consts.formatForDate.format(new Date()) + ". " + ansMsg.getMsg() + "\n");
                } else {
                    textArea.append(Consts.formatForDate.format(new Date()) + ". Something wrong...\n");
                }
            }
        } else if (type.equals(TypeFileActionEnum.GET)) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int ret = fileChooser.showDialog(null, "Select file");
            if (ret == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                try {
                    if (tecNode == null || !tecNode.isLeaf()) {
                        textArea.append(Consts.formatForDate.format(new Date()) + ". File not selected!\n");
                        return;
                    }
                    String pathToFile = selectedPath.toString();
                    FileMessage fm = new FileMessage(pathToFile, TypeFileActionEnum.GET, null, null);
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
                            if (getFile){
                                Network.sendAnswerMessage(clientSocket, null, true, null);
                                textArea.append(Consts.formatForDate.format(new Date()) + ". File: " + pathToFile + " saved on local disk.\n");
                            }else{
                                Network.sendAnswerMessage(clientSocket, null, false, null);
                                textArea.append(Consts.formatForDate.format(new Date()) + ". File: " + pathToFile + " not saved on local disk.\n");
                            }
//                            obj = ois.readObject();
//                            if (obj instanceof AnswerMessage) {
//                                AnswerMessage ansMsg = (AnswerMessage) obj;
//                                if (ansMsg != null) {
//                                    if (ansMsg.isYes()) {
//                                        if (ansMsg.getFiles() != null) refreshTree(ansMsg.getFiles());
//                                    }
//                                    textArea.append(Consts.formatForDate.format(new Date()) + ". " + ansMsg.getMsg() + "\n");
//                                } else {
//                                    textArea.append(Consts.formatForDate.format(new Date()) + ". " + ansMsg.getMsg() + "\n");
//                                }
//                            }
//                            FileMessage inFm = (FileMessage) obj;
//                            if (inFm.getData() != null) {
//                                String localPath = file.getAbsolutePath() + "\\" + inFm.getName();
//                                if (WorkWithFiles.saveFileOnDisk(localPath, inFm)) {
//                                    textArea.append(Consts.formatForDate.format(new Date()) + ". File " + inFm.getName() + " written on: " + file.getAbsolutePath() + "\n");
//                                } else {
//                                    textArea.append(Consts.formatForDate.format(new Date()) + ". Error when write file " + inFm.getName() + "to: " + file.getAbsolutePath() + "...\n");
//                                }
//                            } else {
//                                textArea.append(Consts.formatForDate.format(new Date()) + ". No data from server!\n");
//                            }
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        } else if (newName != null && tecPath == null) {
            String filename = selectedPath.toString();
            if (selectedPath.length() == 0 || tecNode == null || !tecNode.isLeaf()) {// && WorkWithFiles.verifyPath(selectedPath.toString())) {
                textArea.append(Consts.formatForDate.format(new Date()) + ". File not selected!" + "\n");
                return;
            }
            FileMessage fm = new FileMessage(filename, TypeFileActionEnum.RENAME,  newName, null);
            ObjectOutputStream oos = null;
            try {
                oos = new ObjectOutputStream(clientSocket.getOutputStream());
                oos.writeObject(fm);
                oos.flush();
                ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                Object obj = ois.readObject();
                if (obj instanceof AnswerMessage) {
                    AnswerMessage ansmg = (AnswerMessage) obj;
                    if (ansmg.isYes()) {
                        if (ansmg.getFiles() != null) refreshTree(ansmg.getFiles());
                    }
                    textArea.append(ansmg.getMsg() + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } else if (newName != null && tecPath != null) {
            String filename = tecPath;
            FileMessage fm = new FileMessage(filename, TypeFileActionEnum.TRANSFER,  newName, tecPath);
            ObjectOutputStream oos = null;
            try {
                oos = new ObjectOutputStream(clientSocket.getOutputStream());
                oos.writeObject(fm);
                oos.flush();
                ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                Object obj = ois.readObject();
                if (obj instanceof AnswerMessage) {
                    AnswerMessage ansMsg = (AnswerMessage) obj;
                    if (ansMsg.isYes()) {
                        if (ansMsg.getFiles() != null) refreshTree(ansMsg.getFiles());
                    }
                    textArea.append(ansMsg.getMsg() + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } else if (type.equals(TypeFileActionEnum.DELETE)) {

//            if (selectedPath.length() == 0 || WorkWithFiles.verifyPath(selectedPath.toString())) {
//                textArea.append("File not selected!");
//                return;
//            }
            String pathToFile = selectedPath.toString();
            FileMessage fm = new FileMessage(pathToFile, TypeFileActionEnum.DELETE,null, null);
            ObjectOutputStream oos = null;
            try {
                oos = new ObjectOutputStream(clientSocket.getOutputStream());
                oos.writeObject(fm);
                oos.flush();
                ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                AnswerMessage aMsg = (AnswerMessage) ois.readObject();
                textArea.append(aMsg.getMsg() + "\n");
//                textArea.append(formatForDate.format(new Date()) + ". " + aMsg.getMsg() + "\n");
                if (aMsg.isYes()) {
                    if (aMsg.getFiles() != null) refreshTree(aMsg.getFiles());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    private void sendLoginMessage(boolean newUser) {
        if (clientKey != null) {
            StringBuilder sb = new StringBuilder(login.getText() + ";" + password.getText());
            String loginStr = Security.encrypt(sb.toString(), clientKey);
            if (loginStr != null) {
                LoginMessage lm = new LoginMessage(loginStr, newUser);
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                    oos.writeObject(lm);
//                oos.close();
                    oos.flush();
                    ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                    AnswerMessage aMsg = (AnswerMessage) ois.readObject();
                    textArea.append(Consts.formatForDate.format(new Date()) + ". " + aMsg.getMsg() + "\n");
                    if (aMsg.isYes()) {
                        refreshTree(aMsg.getFiles());
                        LogIn = true;
                    }

                } catch (IOException e1) {
                    LogIn = false;
                    e1.printStackTrace();
                    textArea.append(Consts.formatForDate.format(new Date()) + ". " + e1.getLocalizedMessage() + "\n");
                } catch (ClassNotFoundException e) {
                    LogIn = false;
                    e.printStackTrace();
                }
            } else {
                LogIn = false;
                textArea.append(Consts.formatForDate.format(new Date()) + ". Security error! Connection is not allowed...\n");
            }
        } else {
            LogIn = false;
            textArea.append(Consts.formatForDate.format(new Date()) + ". Security key not found! Connection is not allowed...\n");
        }

    }

    private void refreshTree(File[] userFiles) {
        folderList.clear();
        drawTree(login.getText(), userFiles);

    }

    private void drawTree(String root, File[] files) {
        TreeModel model = createTreeModel(root, files);

        tree1.setModel(model);
        tree1.addTreeSelectionListener(new SelectionListener());
        tree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    private TreeModel createTreeModel(Object obj, File[] files) {
        // Корневой узел дерева
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(obj);
        folderList.add(root.toString());
        if (files != null) {
            for (File item : files) {
                if (item.isDirectory()) {
                    addTreeNode(root, item, item.listFiles());
                } else {
                    root.add(new DefaultMutableTreeNode(item.getName(), false));
                }
            }
        }
        // Создание стандартной модели, пустые папки показываются как папки
        return new DefaultTreeModel(root, true);
    }

    private void addTreeNode(DefaultMutableTreeNode root, File item, File[] files) {
        DefaultMutableTreeNode itemNode = new DefaultMutableTreeNode(item.getName(), true);
        StringBuilder sb = new StringBuilder(item.getAbsolutePath());

        if (item.getAbsolutePath().contains(Consts.DIR_PATH)) {
            int lastIndex = Consts.DIR_PATH.length();
            sb = new StringBuilder(item.getAbsolutePath());
            sb.delete(0, lastIndex);
            int index = sb.indexOf("\\");
            sb.replace(0, index, folderList.get(0));
        }

        if (!folderList.contains(sb.toString())) folderList.add(sb.toString());
        itemNode.setAllowsChildren(true);
        root.add(itemNode);

        for (File inItem : files) {
            if (inItem.isDirectory()) {
                addTreeNode(itemNode, inItem, inItem.listFiles());
            } else {
                itemNode.add(new DefaultMutableTreeNode(inItem.getName(), false));
            }
        }

    }

    class SelectionListener implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent e) {
            selectedPath.setLength(0);
            tecNode = null;
            JTree tree = (JTree) e.getSource();
            TreePath[] selected = tree.getSelectionPaths();
            StringBuilder text = new StringBuilder();
            DefaultMutableTreeNode node = null;
            if (selected != null) {
                for (int j = 0; j < selected.length; j++) {
                    TreePath path = selected[j];
                    Object[] nodes = path.getPath();
                    for (int i = 0; i < nodes.length; i++) {
                        node = (DefaultMutableTreeNode) nodes[i];
                        if (i > 0) text.append("\\");
                        text.append(node.getUserObject());
                    }
                    if (text.length() > 0) {
                        tecNode = node;
                        selectedPath.append(text.toString());
                    }
                }
            }
        }
    }

    public String getLogin() {
        return login.getText();
    }

}
