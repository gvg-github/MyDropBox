
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.Socket;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class ClientGUI extends JFrame implements ActionListener {

    private JFrame clientFrame;
    private JTextField login = new JTextField("Login");
    private JTextField password = new JPasswordField("Password");
    private JTextField ipAdress = new JTextField("172.16.172.252");
    private JTextField port = new JTextField("8089");

    private JButton registerButton;
    private JButton connectButton;
    private JButton disconnectButton;

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
    private JTextField selectedFile;
    private JTextArea textArea;
    //    private JTextArea treeArea;
    private JPanel centerPanel;
    private JPanel contents;
    private JTree tree1;

    private StringBuilder selectedPath;
    DefaultMutableTreeNode tecNode;
    ArrayList<String> folderList = new ArrayList<>();

    private Socket clientSocket;
    private SimpleDateFormat formatForDate = new SimpleDateFormat("hh:mm:ss a");

    public static void main(String[] args) {
        new ClientGUI();
    }

    private ClientGUI() {

        selectedPath = new StringBuilder();
        clientFrame = new JFrame("Client window");
        clientFrame.setSize(600, 500);
        clientFrame.setMinimumSize(new Dimension(450, 350));
        clientFrame.setLocation(500, 100);
        clientFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        clientFrame.addWindowListener(new WindowAdapter(this));

        JPanel loginPanel = new JPanel();
        JPanel selectPanel = new JPanel();

        connectButton = new JButton("Connect");
        connectButton.addActionListener(this);

        registerButton = new JButton("Register");
        registerButton.addActionListener(this);

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
        ;
        deleteDirButton.addActionListener(this);

        buttonsPanel = new JPanel(new GridLayout(12, 0, 5, 5));

//        buttonsPanel.setSize(150, 400);
        buttonsPanel.add(new JLabel("Actions with files:"));
        buttonsPanel.add(sendFileButton);
        buttonsPanel.add(getFileButton);
        buttonsPanel.add(renameFileButton);
        buttonsPanel.add(refreshFileButton);
        buttonsPanel.add(transferFileButton);
        buttonsPanel.add(deleteFileButton);
        buttonsPanel.add(new JLabel("Actions with folders:"));
        buttonsPanel.add(createDirButton);
        buttonsPanel.add(renameDirButton);
        buttonsPanel.add(deleteDirButton);


        textArea = new JTextArea(5, 20);

        centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());

        centerPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        loginPanel.add(ipAdress);
        loginPanel.add(port);
        loginPanel.add(login);
        loginPanel.add(password);
        loginPanel.add(connectButton);
        loginPanel.add(registerButton);

        contents = new JPanel(new GridLayout(1, 0));
//        clientFrame.getHeight() - centerPanel.getHeight() - loginPanel.getHeight()
//        contents.setSize(400, 300);
        tree1 = new JTree(createTreeModel("User files will be here...", null));
        contents.add(new JScrollPane(tree1));

        clientFrame.add(loginPanel, BorderLayout.NORTH);
        clientFrame.add(centerPanel, BorderLayout.SOUTH);
        clientFrame.add(contents, BorderLayout.CENTER);
        clientFrame.add(buttonsPanel, BorderLayout.EAST);

        clientFrame.setVisible(true);
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (clientSocket != null) {
            if (e.getSource() == connectButton) {
                sendLoginMessage(false);
            } else if (e.getSource() == registerButton) {
                sendLoginMessage(true);
            } else if (e.getSource() == getFileButton) {
                if (selectedPath.length() == 0 || tecNode == null || !tecNode.isLeaf()) {
                    JOptionPane.showMessageDialog(null, "File not selected!\n", "", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                sendFileMessage(false, true, false, false, null, null);
            } else if (e.getSource() == deleteFileButton) {
                if (selectedPath.length() == 0 || tecNode == null || !tecNode.isLeaf()) {
                    JOptionPane.showMessageDialog(null, "File not selected!\n", "", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int res = JOptionPane.showConfirmDialog(this,"Really delete this file?");
                if (res == JOptionPane.NO_OPTION) return;
                sendFileMessage(false, false, true, false, null, null);

            } else if (e.getSource() == renameFileButton) {
                if (selectedPath.length() == 0 || tecNode == null || !tecNode.isLeaf()) {
                    JOptionPane.showMessageDialog(null, "File not selected!\n", "", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String newFileName = JOptionPane.showInputDialog("Input file name:");
                if (newFileName != null) {
                    sendFileMessage(false, false, false, false, newFileName, null);
                }
            } else if (e.getSource() == transferFileButton) {
                if (selectedPath.length() == 0 || tecNode == null || !tecNode.isLeaf()) {
                    JOptionPane.showMessageDialog(null, "File not selected!\n", "", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                StringBuilder tecFolder = selectedPath;
                if (tecNode.isLeaf()){
                    tecFolder = getFolder(selectedPath.toString());
                }
                String tecPath = selectedPath.toString();


                Object res = JOptionPane.showInputDialog(this, "Select new folder for file:", "", JOptionPane.QUESTION_MESSAGE, null,(Object[]) folderList.toArray(), folderList.get(0));
                if (res == null){
                    JOptionPane.showMessageDialog(null, "New folder not selected!\n", "", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String newFolder = (String) res;
                String newPath = newFolder;

                if (newPath.equals(tecPath) || newFolder.equals(tecFolder.toString())) {
                    JOptionPane.showMessageDialog(null, "New folder not selected!\n", "", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                sendFileMessage(false, false, false, false, newPath, tecPath);
            } else if (e.getSource() == refreshFileButton) {
//                if (selectedPath.length() == 0 || tecNode == null || !tecNode.isLeaf()){
//                    JOptionPane.showMessageDialog(null, "File not selected!\n", "", JOptionPane.WARNING_MESSAGE);
//                    return;
//                }
            } else if (e.getSource() == createDirButton) {
                String newFolderName = JOptionPane.showInputDialog("Input folder name:");
                if (newFolderName != null) {
                    sendFolderMessage(true, false, newFolderName);
                }

            } else if (e.getSource() == renameDirButton) {
                if (selectedPath.length() == 0 || tecNode == null || !tecNode.getAllowsChildren()) {
                    JOptionPane.showMessageDialog(null, "Folder not selected!\n", "", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String newFolderName = JOptionPane.showInputDialog("Input new folder name:");
                if (newFolderName != null) {
                    sendFolderMessage(false, false, newFolderName);
                }
            } else if (e.getSource() == deleteDirButton) {
                if (selectedPath.length() == 0 || tecNode == null || !tecNode.getAllowsChildren()) {
                    JOptionPane.showMessageDialog(null, "Folder not selected!\n", "", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                sendFolderMessage(false, true, null);

            } else if (e.getSource() == sendFileButton) {
                sendFileMessage(true, false, false, false, null, null);
            }

        } else if (e.getSource() == connectButton || e.getSource() == registerButton) {
            try {
                clientSocket = new Socket(ipAdress.getText(), 8089);
                sendLoginMessage(e.getSource() == connectButton ? false : true);
            } catch (IOException e1) {
                textArea.append(formatForDate.format(new Date()) + ". Server not found! \n");
            }
        }
    }

    private void sendFolderMessage(boolean create, boolean delete, String newFolderName) {
        if (create){
            try {
                String folderName = login.getText() + "\\";//  + newFolderName;
                if (selectedPath.length() != 0 && tecNode.getAllowsChildren()) {// && Network.verifyPath(selectedPath.toString())) {
//                        filename = selectedPath.replace(login.getText() + "\\", "") + file.getName();
                    folderName = selectedPath.toString() + "\\";// + newFolderName;
                } else if (tecNode != null && !tecNode.getAllowsChildren()) {
                    int index = selectedPath.lastIndexOf(tecNode.toString());
                    StringBuilder newSelectedPath = selectedPath.delete(index, selectedPath.length());
                    folderName = newSelectedPath.toString() + "\\";// + newFolderName;
                }
                FolderMessage fdm = new FolderMessage(folderName, true, false, newFolderName);
//                    oos.reset();
                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                oos.writeObject(fdm);
                oos.flush();
                ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                Object obj = ois.readObject();
                if (obj instanceof AnswerMessage) {
                    AnswerMessage ansMg = (AnswerMessage) obj;
                    if (ansMg.isYes()) {
                        if (ansMg.getFiles() != null) refreshTree(ansMg.getFiles());
                    }
                    textArea.append(ansMg.getMsg() + "\n");
//                        ois.close();
//                    oos.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }

        }else if (delete){
            try {
                String folderName = login.getText();//  + newFolderName;
                if (selectedPath.length() != 0 && tecNode.getAllowsChildren()) {// && Network.verifyPath(selectedPath.toString())) {
//                        filename = selectedPath.replace(login.getText() + "\\", "") + file.getName();
                    folderName = selectedPath.toString();// + newFolderName;
                } else if (tecNode != null && !tecNode.getAllowsChildren()) {
                    int index = selectedPath.lastIndexOf(tecNode.toString());
                    StringBuilder newSelectedPath = selectedPath.delete(index, selectedPath.length());
                    folderName = newSelectedPath.toString();// + newFolderName;
                }
                FolderMessage fdm = new FolderMessage(folderName, false, true, null);
//                    oos.reset();
                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                oos.writeObject(fdm);
                oos.flush();
                ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                Object obj = ois.readObject();
                if (obj instanceof AnswerMessage) {
                    AnswerMessage ansMg = (AnswerMessage) obj;
                    if (ansMg.isYes()) {
                        if (ansMg.getFiles() != null) refreshTree(ansMg.getFiles());
                    }
                    textArea.append(ansMg.getMsg() + "\n");
//                        ois.close();
//                    oos.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }
        }else{
            try {
                String folderName = selectedPath.toString() + "\\";
                FolderMessage fdm = new FolderMessage(folderName, false, false, newFolderName);
//                    oos.reset();
                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                oos.writeObject(fdm);
                oos.flush();
                ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                Object obj = ois.readObject();
                if (obj instanceof AnswerMessage) {
                    AnswerMessage ansMg = (AnswerMessage) obj;
                    if (ansMg.isYes()) {
                        if (ansMg.getFiles() != null) refreshTree(ansMg.getFiles());
                    }
                    textArea.append(ansMg.getMsg() + "\n");
//                        ois.close();
//                    oos.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }
        }
    }

    private StringBuilder getFolder(String tecName){
        StringBuilder nameBuilder = new StringBuilder(selectedPath);

        int index1 = tecName.lastIndexOf("\\");
        nameBuilder.delete(index1, nameBuilder.length());
        return nameBuilder;
    }

    private void sendFileMessage(boolean send, boolean get, boolean delete, boolean refresh, String newName, String tecPath) {
        if (send) {
            JFileChooser fileChooser = new JFileChooser();
            int ret = fileChooser.showDialog(null, "Select file");
            if (ret == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                try {
                    String filename = login.getText() + "\\" + file.getName();
                    if (selectedPath.length() != 0 && tecNode.getAllowsChildren()) {// && Network.verifyPath(selectedPath.toString())) {
//                        filename = selectedPath.replace(login.getText() + "\\", "") + file.getName();
                        filename = selectedPath.toString() + "\\" + file.getName();
                    } else if (tecNode != null && !tecNode.getAllowsChildren()) {
                        int index = selectedPath.lastIndexOf(tecNode.toString());
                        StringBuilder newSelectedPath = selectedPath.delete(index, selectedPath.length());
                        filename = newSelectedPath.toString() + "\\" + file.getName();
                    }
                    FileMessage fm = new FileMessage(filename, Files.readAllBytes(Paths.get(file.getAbsolutePath())), false, false, null, null);
//                    oos.reset();
                    ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                    oos.writeObject(fm);
                    oos.flush();
                    ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                    Object obj = ois.readObject();
                    if (obj instanceof AnswerMessage) {
                        AnswerMessage ansmg = (AnswerMessage) obj;
                        if (ansmg.isYes()) {
                            if (ansmg.getFiles() != null) refreshTree(ansmg.getFiles());
                        }
                        textArea.append(formatForDate.format(new Date()) + ". " + ansmg.getMsg() + "\n");
//                        ois.close();
//                    oos.close();
                    }
//                    String returnMsg = dis.readUTF();
//                    textArea.append(returnMsg + "\n");
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (ClassNotFoundException e1) {
                    e1.printStackTrace();
                }
            }
        } else if (get) {

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int ret = fileChooser.showDialog(null, "Select file");
            if (ret == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                try {
                    if (tecNode == null || !tecNode.isLeaf()) {
                        textArea.append(formatForDate.format(new Date()) + ". File not selected!\n");
                        return;
                    }
                    String pathToFile = selectedPath.toString();
                    FileMessage fm = new FileMessage(pathToFile, null, false, false, null, null);
//                    oos.reset();
                    ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                    oos.writeObject(fm);
//                    oos.close();
                    oos.flush();
                    ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                    Object obj = null;
                    try {
                        obj = ois.readObject();
                        if (obj instanceof FileMessage) {
                            FileMessage inFm = (FileMessage) obj;
                            if (inFm.getData() != null) {
                                String localPath = file.getAbsolutePath() + "\\" + inFm.getName();
                                if (Network.saveFileOnDisk(localPath, inFm)) {
                                    textArea.append(formatForDate.format(new Date()) + ". File " + inFm.getName() + " written on: " + file.getAbsolutePath() + "\n");
                                } else {
                                    textArea.append(formatForDate.format(new Date()) + ". Error when write file " + inFm.getName() + "to: " + file.getAbsolutePath() + "...\n");
                                }
                            } else {
                                textArea.append(formatForDate.format(new Date()) + ". No data from server!\n");
                            }
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
            if (selectedPath.length() == 0 || tecNode == null || !tecNode.isLeaf()) {// && Network.verifyPath(selectedPath.toString())) {
                textArea.append(formatForDate.format(new Date()) + ". File not selected!" + "\n");
                return;
            }
            FileMessage fm = new FileMessage(filename, null, false, false, newName, null);
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
            FileMessage fm = new FileMessage(filename, null, false, false, newName, tecPath);
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

        } else if (delete) {

//            if (selectedPath.length() == 0 || Network.verifyPath(selectedPath.toString())) {
//                textArea.append("File not selected!");
//                return;
//            }
            String pathToFile = selectedPath.toString();
            FileMessage fm = new FileMessage(pathToFile, null, true, false, null, null);
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
        LoginMessage lm = new LoginMessage(login.getText(), password.getText(), newUser);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
            oos.writeObject(lm);
//                oos.close();
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
            AnswerMessage aMsg = (AnswerMessage) ois.readObject();
            textArea.append(formatForDate.format(new Date()) + ". " + aMsg.getMsg() + "\n");
            if (aMsg.isYes()) refreshTree(aMsg.getFiles());

        } catch (IOException e1) {
            e1.printStackTrace();
            textArea.append(formatForDate.format(new Date()) + ". " + e1.getLocalizedMessage() + "\n");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void refreshTree(File[] userFiles) {
        folderList.clear();
        drawTree(login.getText(), userFiles);
//        if (returnMsg.contains(findString.subSequence(0, findString.length() - 1))) {
//            drawTree(login.getText(), userFiles);
//        }
    }

    private void drawTree(String root, File[] files) {
        TreeModel model = createTreeModel(root, files);

//        tree1.clearSelection();
        tree1.setModel(model);
        tree1.addTreeSelectionListener(new SelectionListener());

//        JTree tree1 = new JTree(model);
//        TreeSelectionModel selModel = new DefaultTreeSelectionModel();
//        selModel.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        // Подключение моделей выделения
        tree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
//        JPanel contents = new JPanel(new GridLayout(1, 1));
        // Размещение деревьев в интерфейсе
//        contents.add(new JScrollPane(tree1));
//        clientFrame.getContentPane().add(contents, BorderLayout.CENTER);
//        // Размещение текстового поля в нижней части интерфейса
//        clientFrame.getContentPane().add(new JScrollPane(textArea), BorderLayout.SOUTH);
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
//            // Добавление ветвей - потомков 1-го уровня
//            DefaultMutableTreeNode drink = new DefaultMutableTreeNode(nodes[0]);
//            DefaultMutableTreeNode sweet = new DefaultMutableTreeNode(nodes[1]);
//            // Добавление ветвей к корневой записи
//            root.add(drink);
//            root.add(sweet);
//            // Добавление листьев - потомков 2-го уровня
//            for (int i = 0; i < leafs[0].length; i++)
//                drink.add(new DefaultMutableTreeNode(leafs[0][i], false));
//            for (int i = 0; i < leafs[1].length; i++)
//                sweet.add(new DefaultMutableTreeNode(leafs[1][i], false));
        }
        // Создание стандартной модели
        return new DefaultTreeModel(root);
    }

    private void addTreeNode(DefaultMutableTreeNode root, File item, File[] files) {
        DefaultMutableTreeNode itemNode = new DefaultMutableTreeNode(item.getName(), true);
        StringBuilder sb = new StringBuilder(item.getAbsolutePath());
        if (item.getAbsolutePath().contains(folderList.get(0))) {
            int index = item.getAbsolutePath().indexOf(folderList.get(0));
            sb = new StringBuilder(item.getAbsolutePath());
            sb.delete(0, index);
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
            JTree tree = (JTree) e.getSource();
            TreePath[] selected = tree.getSelectionPaths();
//            int[] rows = tree.getSelectionRows();
            StringBuilder text = new StringBuilder();
            DefaultMutableTreeNode node = null;
            if (selected != null) {
                for (int j = 0; j < selected.length; j++) {
                    TreePath path = selected[j];
                    Object[] nodes = path.getPath();
                    for (int i = 0; i < nodes.length; i++) {

//                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes[i];
                        node = (DefaultMutableTreeNode) nodes[i];
                        if (i > 0) text.append("\\");
//                        if (!node.isLeaf()) text.append(node.getUserObject());
                        text.append(node.getUserObject());
                    }
                    if (text.length() > 0) {
//                        if (text.charAt(text.length() - 1) != '\\') text.append("\\");
                        tecNode = node;
                        selectedPath.append(text.toString());
//                        text.append("\n");
//                        textArea.append(text.toString());
                    }
                }
            }
        }
    }

    public String getLogin() {
        return login.getText();
    }


}
