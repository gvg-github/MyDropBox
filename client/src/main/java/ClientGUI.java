import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

import java.util.ArrayList;
import java.util.Date;

import static java.lang.Thread.sleep;


class ClientGUI extends JFrame {

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

    private File file;
    private JTextArea textArea;
    private JPanel centerPanel;
    private JPanel contents;
    private JTree tree1;

    private StringBuilder selectedPath;
    private DefaultMutableTreeNode tecNode;
    private ArrayList<String> folderList = new ArrayList<>();

    private Socket clientSocket;
    private String login;
    private int userSize;
    private JProgressBar sizeBar;
    private TimerLabel timerLabel;

    private MyProgressBar_Simple pBar;

    public ClientGUI(Socket clientSocket, File[] userFiles, int userSize, String login) {

        this();
        this.clientSocket = clientSocket;
        this.login = login;
        this.userSize = convertToMb(userSize);
        refreshTree(userFiles);
    }

    public ClientGUI() {

        super();
        selectedPath = new StringBuilder();

        setTitle("Client window");
        setSize(600, 600);
        setMinimumSize(new Dimension(300, 300));

        int[] coords = ClientStartFrame.getStartCoords(this);
        setLocation((int) (coords[0] - getWidth()) / 2, (int) (coords[1] - getHeight()) / 2);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new ClientGUI_WindowAdapter(this));

        timerLabel = new TimerLabel(this);
        timerLabel.setFont(new Font(timerLabel.getFont().getFontName(), timerLabel.getFont().getStyle(), 25));

        JLabel textTimer = new JLabel("Until the end of session remained:");
        textTimer.setFont(new Font(textTimer.getFont().getFontName(), textTimer.getFont().getStyle(), 20));
        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        loginPanel.add(textTimer);
        loginPanel.add(timerLabel);

        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new GUI_Listener(this));
        //Правая панель с кнопками для работы с файлами и папками
        getIdButton = new JButton("Get file ID");
        getIdButton.setToolTipText("Hot key \"I\"");
        getIdButton.addActionListener(new GUI_Listener(this));

        sendFileButton = new JButton("Send file to storage");
        sendFileButton.setToolTipText("Hot key \"S\"");
        sendFileButton.addActionListener(new GUI_Listener(this));

        getFileButton = new JButton("Get selected file");
        getFileButton.setToolTipText("Hot key \"G\"");
        getFileButton.addActionListener(new GUI_Listener(this));

        deleteFileButton = new JButton("Delete selected file");
        deleteFileButton.setToolTipText("Hot key \"Del\"");
        deleteFileButton.addActionListener(new GUI_Listener(this));

        refreshFileButton = new JButton("Refresh selected file");
        refreshFileButton.setToolTipText("Hot key \"Alt\" + \"R\"");
        refreshFileButton.addActionListener(new GUI_Listener(this));

        renameFileButton = new JButton("Rename selected file");
        renameFileButton.setToolTipText("Hot key \"R\"");
        renameFileButton.addActionListener(new GUI_Listener(this));

        transferFileButton = new JButton("Transfer selected file");
        transferFileButton.setToolTipText("Hot key \"T\"");
        transferFileButton.addActionListener(new GUI_Listener(this));

        createDirButton = new JButton("Create folder");
        createDirButton.setToolTipText("Hot key \"Ctrl\" + \"C\"");
        createDirButton.addActionListener(new GUI_Listener(this));

        renameDirButton = new JButton("Rename selected folder");
        renameDirButton.setToolTipText("Hot key \"Ctrl\" + \"R\"");
        renameDirButton.addActionListener(new GUI_Listener(this));

        deleteDirButton = new JButton("Delete selected folder");
        deleteDirButton.setToolTipText("Hot key \"Ctrl\" + \"Del\"");
        deleteDirButton.addActionListener(new GUI_Listener(this));

        buttonsPanel = new JPanel(new GridLayout(13, 0, 5, 5));
        buttonsPanel.setMinimumSize(buttonsPanel.getSize());
        buttonsPanel.setMaximumSize(buttonsPanel.getSize());
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

        contents = new JPanel(new BorderLayout());
        sizeBar = new JProgressBar();
        sizeBar.setStringPainted(true);
        sizeBar.setString("Used: " + userSize + " МВ from: " + Consts.USER_SIZE + " MB.");
        sizeBar.setMinimum(0);
        sizeBar.setMaximum(Consts.USER_SIZE);
        sizeBar.setValue(userSize);
        sizeBar.setBorder(BorderFactory.createBevelBorder(1));

        //Дерево файлов пользователя
        tree1 = new JTree(createTreeModel("User files will be here...", null));
        contents.add(new JScrollPane(tree1), BorderLayout.CENTER);
        contents.add(sizeBar, BorderLayout.NORTH);
        contents.setBorder(BorderFactory.createBevelBorder(1));

        pBar = new MyProgressBar_Simple();
        pBar.showBar();

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

        try {
            clientSocket.close();
            clientSocket = null;
            setVisible(false);
            if (msg != null) {
                new ClientStartFrame(msg);
            } else {
                new ClientStartFrame();
            }
            dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    void restartTimer() {
        timerLabel.restartTimer();
    }

    void sendFolderMessage(boolean create, boolean delete) {
        AnswerMessage ansMsg = null;
        String folderName = null;
        if (create) {
            String newFolderName = JOptionPane.showInputDialog("Input folder name:");
            if (newFolderName != null) {
                folderName = getPathToFolder(true, false);
                ansMsg = Network.sendFolderMessage(clientSocket, folderName, true, false, newFolderName);
            }
        } else if (delete) {
            folderName = getPathToFolder(false, true);
            ansMsg = Network.sendFolderMessage(clientSocket, folderName, false, true, null);

        } else {
            String newFolderName = JOptionPane.showInputDialog("Input new folder name:");
            if (newFolderName != null) {
                folderName = selectedPath.toString() + "\\";
                ansMsg = Network.sendFolderMessage(clientSocket, folderName, false, false, newFolderName);
            }
        }

        if (ansMsg != null) {
            handleAnswerMessage(ansMsg);
        }
    }

    void sendFileMessage(FileActionEnum type, String newName, String tecPath) {

        if (type.equals(FileActionEnum.SEND)) {
            AnswerMessage ansMsg = null;
            JFileChooser fileChooser = new JFileChooser();
            int ret = fileChooser.showDialog(this, "Select file");
            if (ret == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                int freeSpace = Consts.USER_SIZE - userSize;
                int fileSize = convertToMb((int) file.length());
                if (fileSize > freeSpace) {
                    JOptionPane.showMessageDialog(this, "File size: " + fileSize + " MB, free space: " + freeSpace + " MB. Transfer canselled.", "", JOptionPane.WARNING_MESSAGE);
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

                pBar.showBar();
                try {
                    ansMsg = Network.sendFile(file, filename, clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                pBar.hideBar();

                if (ansMsg != null) {
                    handleAnswerMessage(ansMsg);
                } else {
                    textArea.append(Consts.formatForDate.format(new Date()) + ". Something wrong...\n");
                }
            }

        } else if (type.equals(FileActionEnum.GET)) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int ret = fileChooser.showDialog(this, "Select file");
            if (ret == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                try {
                    if (tecNode == null || !tecNode.isLeaf()) {
                        textArea.append(Consts.formatForDate.format(new Date()) + ". File not selected!\n");
                        return;
                    }

                    pBar.showBar();
                    String pathToFile = selectedPath.toString();
                    FileMessage fm = new FileMessage(pathToFile, FileActionEnum.GET, null, null);
                    try {
                        Object obj = messaginWithServer(fm, clientSocket);
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
                    pBar.hideBar();
                } catch (IOException e) {
                    e.printStackTrace();
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
                try {
                    Object obj = messaginWithServer(fm, clientSocket);
                    if (obj instanceof AnswerMessage) {
                        AnswerMessage ansMsg = (AnswerMessage) obj;
                        String selectFileID = ansMsg.getMsg();
                        textArea.append(Consts.formatForDate.format(new Date()) + ". File ID: " + selectFileID + "\n");
                        JOptionPane.showMessageDialog(this, "File ID: " + selectFileID + "\n", "", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (type.equals(FileActionEnum.RENAME)) {
            String filename = selectedPath.toString();
            if (selectedPath.length() == 0 || tecNode == null || !tecNode.isLeaf()) {
                textArea.append(Consts.formatForDate.format(new Date()) + ". File not selected!" + "\n");
                return;
            }
            FileMessage fm = new FileMessage(filename, FileActionEnum.RENAME, newName, null);
            try {
                Object obj = messaginWithServer(fm, clientSocket);

                if (obj instanceof AnswerMessage) {
                    AnswerMessage ansMsg = (AnswerMessage) obj;
                    handleAnswerMessage(ansMsg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } else if (type.equals(FileActionEnum.TRANSFER)) {
            String filename = tecPath;
            FileMessage fm = new FileMessage(filename, FileActionEnum.TRANSFER, newName, tecPath);
            try {
                Object obj = messaginWithServer(fm, clientSocket);

                if (obj instanceof AnswerMessage) {
                    AnswerMessage ansMsg = (AnswerMessage) obj;
                    handleAnswerMessage(ansMsg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            pBar.hideBar();

        } else if (type.equals(FileActionEnum.DELETE)) {
            String pathToFile = selectedPath.toString();
            FileMessage fm = new FileMessage(pathToFile, FileActionEnum.DELETE, null, null);
            try {
                Object obj = messaginWithServer(fm, clientSocket);
                AnswerMessage aMsg = (AnswerMessage) obj;
                handleAnswerMessage(aMsg);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } else if (type.equals(FileActionEnum.REFRESH)) {
            if (selectedPath.length() != 0 && tecNode.getAllowsChildren()) {
                return;
            }
            pBar.showBar();
            FileMessage fm = new FileMessage(tecPath, FileActionEnum.REFRESH, null, null);
            try {
                Object obj = messaginWithServer(fm, clientSocket);
                AnswerMessage aMsg = (AnswerMessage) obj;
                if (aMsg.isYes()) {
                    textArea.append(aMsg.getMsg() + "\n");
                    file = new File(aMsg.getMsg());
                    String filename = login + "\\" + file.getName();
                    if (selectedPath.length() != 0 && tecNode.getAllowsChildren()) {
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
                            handleAnswerMessage(ansMsg);
                        } else {
                            textArea.append(Consts.formatForDate.format(new Date()) + ". Something wrong...\n");
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            pBar.hideBar();
        }
    }

    private Object messaginWithServer(FileMessage fm, Socket clientSocket) throws IOException, ClassNotFoundException {
        ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
        oos.writeObject(fm);
        oos.flush();
        ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
        return ois.readObject();
    }

    private void handleAnswerMessage(AnswerMessage ansMsg) {
        if (ansMsg.isYes()) {
            if (ansMsg.getFiles() != null) {
                userSize = convertToMb(ansMsg.getSize());
                refreshTree(ansMsg.getFiles());
            }
        }
        textArea.append(Consts.formatForDate.format(new Date()) + ". " + ansMsg.getMsg() + "\n");
    }

    private String getPathToFolder(boolean create, boolean delete) {
        String folderName = null;
        if (create) {
            folderName = login + "\\";
            if (selectedPath.length() != 0 && tecNode.getAllowsChildren()) {
                folderName = selectedPath.toString() + "\\";
            } else if (tecNode != null && !tecNode.getAllowsChildren()) {
                int index = selectedPath.lastIndexOf(tecNode.toString());
                StringBuilder newSelectedPath = selectedPath.delete(index, selectedPath.length());
                folderName = newSelectedPath.toString() + "\\";
            }
        } else if (delete) {
            folderName = login;
            if (selectedPath.length() != 0 && tecNode.getAllowsChildren()) {
                folderName = selectedPath.toString();
            } else if (tecNode != null && !tecNode.getAllowsChildren()) {
                int index = selectedPath.lastIndexOf(tecNode.toString());
                StringBuilder newSelectedPath = selectedPath.delete(index, selectedPath.length());
                folderName = newSelectedPath.toString();
            }
        }
        return folderName;
    }

    StringBuilder getFolder(String tecName) {
        StringBuilder nameBuilder = new StringBuilder(selectedPath);

        int index1 = tecName.lastIndexOf("\\");
        nameBuilder.delete(index1, nameBuilder.length());
        return nameBuilder;
    }

    private void refreshTree(File[] userFiles) {
        folderList.clear();
        drawTree(login, userFiles);
        sizeBar.setValue(userSize);
        sizeBar.setString("Used: " + userSize + " МВ from: " + Consts.USER_SIZE + " MB.");
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

    public StringBuilder getSelectedPath() {
        return selectedPath;
    }

    public DefaultMutableTreeNode getTecNode() {
        return tecNode;
    }

    public String getLogin() {
        return login;
    }

    public JButton getSendFileButton() {
        return sendFileButton;
    }

    public JButton getGetIdButton() {
        return getIdButton;
    }

    public JButton getGetFileButton() {
        return getFileButton;
    }

    public JButton getDeleteFileButton() {
        return deleteFileButton;
    }

    public JButton getRefreshFileButton() {
        return refreshFileButton;
    }

    public JButton getRenameFileButton() {
        return renameFileButton;
    }

    public JButton getTransferFileButton() {
        return transferFileButton;
    }

    public JButton getCreateDirButton() {
        return createDirButton;
    }

    public JButton getRenameDirButton() {
        return renameDirButton;
    }

    public JButton getDeleteDirButton() {
        return deleteDirButton;
    }

    public ArrayList<String> getFolderList() {
        return folderList;
    }
}
