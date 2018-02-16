import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;

import static java.lang.Thread.sleep;


class ClientGUI extends JFrame implements ActionListener {

    private JButton sendFileButton;
    private JButton getIdButton;
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

    private Socket clientSocket;
    private boolean LogIn;
    private ObjectOutputStream oos;
    private String login;
    private int userSize;
    private JProgressBar sizeBar;
    private TimerLabel timerLabel;

    private MyProgressBar pBar;

    public ClientGUI(MyProgressBar pBar, Socket clientSocket, File[] userFiles, int userSize, String login) {
        this();
        this.pBar = pBar;
        this.clientSocket = clientSocket;
        this.login = login;
        this.LogIn = true;
        this.userSize = convertToMb(userSize);
        refreshTree(userFiles);
    }

    public ClientGUI() {

        super();
        selectedPath = new StringBuilder();

        setTitle("Client window");
        setSize(600, 600);
        setMinimumSize(new Dimension(300, 300));
        setLocation(500, 100);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new ClientGUI_WindowAdapter(this));

//        Font otherFont = new Font("TimesRoman", Font.BOLD, 15);

        timerLabel = new TimerLabel(this);
        timerLabel.setFont(new Font(timerLabel.getFont().getFontName(), timerLabel.getFont().getStyle(), 25));

        JLabel textTimer = new JLabel("Until the end of session remained:");
        textTimer.setFont(new Font(textTimer.getFont().getFontName(), textTimer.getFont().getStyle(), 20));
        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        loginPanel.add(textTimer);
        loginPanel.add(timerLabel);

        //Правая панель с кнопками для работы с файлами и папками
        getIdButton = new JButton("Get file ID");
        getIdButton.addActionListener(this);
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

        buttonsPanel = new JPanel(new GridLayout(13, 0, 5, 5));
        JLabel fileActions = new JLabel("Actions with files:");
        fileActions.setBorder(BorderFactory.createBevelBorder(0));
        buttonsPanel.add(fileActions);
        buttonsPanel.add(sendFileButton);
        buttonsPanel.add(getFileButton);
        buttonsPanel.add(getIdButton);
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
        contents = new JPanel(new BorderLayout());
        sizeBar = new JProgressBar();
        sizeBar.setName("Size used:");
        sizeBar.setStringPainted(true);
        sizeBar.setMinimum(0);
        sizeBar.setMaximum(Consts.USER_SIZE);
        sizeBar.setValue(userSize);
        sizeBar.setBorder(BorderFactory.createBevelBorder(1));

        tree1 = new JTree(createTreeModel("User files will be here...", null));
        contents.add(new JScrollPane(tree1), BorderLayout.CENTER);
        contents.add(sizeBar, BorderLayout.NORTH);
        contents.setBorder(BorderFactory.createBevelBorder(1));

        //Вывод на фрейм
        add(loginPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.SOUTH);
        add(contents, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.EAST);

        setMinimumSize(getSize());
        setVisible(true);

    }

    private int convertToMb(int userSize) {
        return (userSize / (1024 * 1024));
    }

    public void closeClientFrame(String msg) {

//        clientFrame.setVisible(false);
        try {
            clientSocket.close();
            clientSocket = null;
            setVisible(false);
            if (msg != null) {
                new ClientStartFrame(msg);
            } else {
                new ClientStartFrame();
            }
//        clientFrame.dispose();
            dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    private void restartTimer() {
        timerLabel.restartTimer();

//        timerLabel.setFont(new Font(timerLabel.getFont().getFontName(), timerLabel.getFont().getStyle(), 25));
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        restartTimer();
        if (clientSocket != null && !clientSocket.isClosed()) {
            if (LogIn) {
                if (e.getSource() == getFileButton) {
                    if (selectedPath.length() == 0 || tecNode == null || !tecNode.isLeaf()) {
                        JOptionPane.showMessageDialog(null, "File not selected!\n", "", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    sendFileMessage(FileActionEnum.GET, null, null);

                } else if (e.getSource() == getIdButton) {
                    if (selectedPath.length() == 0 || tecNode == null || !tecNode.isLeaf()) {
                        JOptionPane.showMessageDialog(null, "File not selected!\n", "", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    sendFileMessage(FileActionEnum.GET_ID, null, null);

                } else if (e.getSource() == deleteFileButton) {
                    if (selectedPath.length() == 0 || tecNode == null || !tecNode.isLeaf()) {
                        JOptionPane.showMessageDialog(null, "File not selected!\n", "", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    int res = JOptionPane.showConfirmDialog(this, "Really delete this file?");
                    if (res == JOptionPane.NO_OPTION) return;
                    ;
                    sendFileMessage(FileActionEnum.DELETE, null, null);

                } else if (e.getSource() == renameFileButton) {
                    if (selectedPath.length() == 0 || tecNode == null || !tecNode.isLeaf()) {
                        JOptionPane.showMessageDialog(null, "File not selected!\n", "", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    String newFileName = JOptionPane.showInputDialog("Input file name:");
                    if (newFileName != null) {
                        sendFileMessage(FileActionEnum.RENAME, newFileName, null);
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
                    sendFileMessage(FileActionEnum.TRANSFER, newPath, tecPath);

                } else if (e.getSource() == refreshFileButton) {
                    if (selectedPath.length() == 0 || tecNode == null || !tecNode.isLeaf()) {
                        JOptionPane.showMessageDialog(null, "File not selected!\n", "", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    String tecPath = selectedPath.toString();
                    sendFileMessage(FileActionEnum.REFRESH, null, tecPath);

                } else if (e.getSource() == sendFileButton) {
                    sendFileMessage(FileActionEnum.SEND, null, null);

                } else if (e.getSource() == createDirButton) {
                    sendFolderMessage(true, false);
                } else if (e.getSource() == renameDirButton) {
                    sendFolderMessage(false, false);
                } else if (e.getSource() == deleteDirButton) {
                    sendFolderMessage(false, true);
                }
            }
        } else {
            closeClientFrame("Socket closed!");
        }
    }

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
                if (ansMsg.getFiles() != null) {
                    userSize = convertToMb(ansMsg.getSize());
                    refreshTree(ansMsg.getFiles());
                }
            }
            textArea.append(ansMsg.getMsg() + "\n");
//                        ois.close();
//                    oos.close();
        }
    }

    private void sendFileMessage(FileActionEnum type, String newName, String tecPath) {

        if (type.equals(FileActionEnum.SEND)) {
            AnswerMessage ansMsg = null;
            JFileChooser fileChooser = new JFileChooser();
            int ret = fileChooser.showDialog(null, "Select file");
            if (ret == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                int freeSpace = Consts.USER_SIZE - userSize;
                int fileSize = convertToMb((int) file.length());
                if (fileSize > freeSpace) {
                    JOptionPane.showMessageDialog(null, "File size: " + fileSize + " MB, free space: " + freeSpace + " MB. Transfer canselled.", "", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String filename = login + "\\" + file.getName();
                if (selectedPath.length() != 0 && tecNode.getAllowsChildren()) {
                    filename = selectedPath.toString() + "\\" + file.getName();
                } else if (tecNode != null && !tecNode.getAllowsChildren()) {
                    int index = selectedPath.lastIndexOf(tecNode.toString());
                    StringBuilder newSelectedPath = selectedPath.delete(index, selectedPath.length());
                    filename = newSelectedPath.toString() + "\\" + file.getName();
                }
                pBar.setShowBar(true);
//                showBar = true;
//                showProgressBar();
//                MessageSender messageThread = new MessageSender(file, filename, clientSocket);
//                messageThread.start();
//                ansMsg = messageThread.getaMsg();
                try {
                    ansMsg = Network.sendFile(file, filename, clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
//                showBar = false;
                pBar.setShowBar(false);
                if (ansMsg != null) {
                    if (ansMsg.isYes()) {
                        if (ansMsg.getFiles() != null) {
                            userSize = convertToMb(ansMsg.getSize());
                            refreshTree(ansMsg.getFiles());
                        }
                    }
                    textArea.append(Consts.formatForDate.format(new Date()) + ". " + ansMsg.getMsg() + "\n");
                } else {
                    textArea.append(Consts.formatForDate.format(new Date()) + ". Something wrong...\n");
                }
            }
        } else if (type.equals(FileActionEnum.GET)) {
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
                    FileMessage fm = new FileMessage(pathToFile, FileActionEnum.GET, null, null);
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
                                textArea.append(Consts.formatForDate.format(new Date()) + ". File: " + pathToFile + " saved on local disk.\n");
                            } else {
                                Network.sendAnswerMessage(clientSocket, null, false, null);
                                textArea.append(Consts.formatForDate.format(new Date()) + ". File: " + pathToFile + " not saved on local disk.\n");
                            }

                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } else if (type.equals(FileActionEnum.GET_ID)) {
            try {
                if (tecNode == null || !tecNode.isLeaf()) {
                    textArea.append(Consts.formatForDate.format(new Date()) + ". File not selected!\n");
                    return;
                }
                String pathToFile = selectedPath.toString();
                FileMessage fm = new FileMessage(pathToFile, FileActionEnum.GET_ID, null, null);
                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                oos.writeObject(fm);
                oos.flush();
                ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                Object obj = null;
                try {
                    obj = ois.readObject();
                    if (obj instanceof AnswerMessage) {
                        AnswerMessage ansMsg = (AnswerMessage) obj;
                        String selectFileID = ansMsg.getMsg();
                        textArea.append(Consts.formatForDate.format(new Date()) + ". File ID: " + selectFileID + "\n");
                        JOptionPane.showMessageDialog(null, "File ID: " + selectFileID + "\n", "", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } else if (type.equals(FileActionEnum.RENAME)) {
//        } else if (newName != null && tecPath == null) {
            String filename = selectedPath.toString();
            if (selectedPath.length() == 0 || tecNode == null || !tecNode.isLeaf()) {// && WorkWithFiles.verifyPath(selectedPath.toString())) {
                textArea.append(Consts.formatForDate.format(new Date()) + ". File not selected!" + "\n");
                return;
            }
            FileMessage fm = new FileMessage(filename, FileActionEnum.RENAME, newName, null);
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
                        if (ansMsg.getFiles() != null) {
                            userSize = convertToMb(ansMsg.getSize());
                            refreshTree(ansMsg.getFiles());
                        }
                    }
                    textArea.append(ansMsg.getMsg() + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } else if (type.equals(FileActionEnum.TRANSFER)) {
//        } else if (newName != null && tecPath != null) {
            String filename = tecPath;
            FileMessage fm = new FileMessage(filename, FileActionEnum.TRANSFER, newName, tecPath);
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
                        if (ansMsg.getFiles() != null) {
                            userSize = convertToMb(ansMsg.getSize());
                            refreshTree(ansMsg.getFiles());
                        }
                    }
                    textArea.append(ansMsg.getMsg() + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } else if (type.equals(FileActionEnum.DELETE)) {
            String pathToFile = selectedPath.toString();
            FileMessage fm = new FileMessage(pathToFile, FileActionEnum.DELETE, null, null);
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
                    if (aMsg.getFiles() != null) {
                        userSize = convertToMb(aMsg.getSize());
                        refreshTree(aMsg.getFiles());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else if (type.equals(FileActionEnum.REFRESH)) {

            if (selectedPath.length() != 0 && tecNode.getAllowsChildren()) {
                return;
            }
            FileMessage fm = new FileMessage(tecPath, FileActionEnum.REFRESH, null, null);
            ObjectOutputStream oos = null;
            try {
                oos = new ObjectOutputStream(clientSocket.getOutputStream());
                oos.writeObject(fm);
                oos.flush();
                ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                AnswerMessage aMsg = (AnswerMessage) ois.readObject();
                if (aMsg.isYes()) {
                    textArea.append(aMsg.getMsg() + "\n");
                    file = new File(aMsg.getMsg());
                    String filename = login + "\\" + file.getName();
                    if (selectedPath.length() != 0 && tecNode.getAllowsChildren()) {// && WorkWithFiles.verifyPath(selectedPath.toString())) {
                        filename = selectedPath.toString() + "\\" + file.getName();
                    } else if (tecNode != null && !tecNode.getAllowsChildren()) {
                        int index = selectedPath.lastIndexOf(tecNode.toString());
                        if (index != -1) {
                            StringBuilder newSelectedPath = selectedPath.delete(index, selectedPath.length());
                            filename = newSelectedPath.toString() + file.getName();
                        } else {
                            filename = selectedPath.toString() + file.getName();
                        }
                    }
                    if (file.exists() && file.isFile()) {
                        AnswerMessage ansMsg = Network.sendFile(file, filename, clientSocket);
                        if (ansMsg != null) {
                            if (ansMsg.isYes()) {
                                if (ansMsg.getFiles() != null) {
                                    userSize = convertToMb(ansMsg.getSize());
                                    refreshTree(ansMsg.getFiles());
                                }
                            }
                            textArea.append(Consts.formatForDate.format(new Date()) + ". " + ansMsg.getMsg() + "\n");
                        } else {
                            textArea.append(Consts.formatForDate.format(new Date()) + ". Something wrong...\n");
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    private String getPathToFolder(boolean create, boolean delete) {
        String folderName = null;
        if (create) {
//            folderName = login.getText() + "\\";//  + newFolderName;
            folderName = login + "\\";//  + newFolderName;
            if (selectedPath.length() != 0 && tecNode.getAllowsChildren()) {// && WorkWithFiles.verifyPath(selectedPath.toString())) {
                folderName = selectedPath.toString() + "\\";// + newFolderName;
            } else if (tecNode != null && !tecNode.getAllowsChildren()) {
                int index = selectedPath.lastIndexOf(tecNode.toString());
                StringBuilder newSelectedPath = selectedPath.delete(index, selectedPath.length());
                folderName = newSelectedPath.toString() + "\\";// + newFolderName;
            }
        } else if (delete) {
//            folderName = login.getText();//  + newFolderName;
            folderName = login;//  + newFolderName;
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

    private void refreshTree(File[] userFiles) {
        folderList.clear();
//        drawTree(login.getText(), userFiles);
        drawTree(login, userFiles);
        sizeBar.setValue(userSize);

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
        return login;
    }

}
