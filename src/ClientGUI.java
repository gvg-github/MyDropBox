import sun.nio.ch.Net;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class ClientGUI implements ActionListener {

    private JFrame clientFrame;
    private JTextField login = new JTextField("Login");
    private JTextField password = new JPasswordField("Password");
    private JButton connectButton;
    private JButton registerButton;
    private JButton selectButton;
    private File file;
    private JTextField selectedFile;
    private JTextArea textArea;

    private Socket clientSocket;
//    private ObjectOutputStream oos;
//    private ObjectInputStream ois;

    private String[] nodes = new String[]{"1", "2"};
    private String[][] leafs = new String[][]{{"Инст.pdf", "Стенд .pdf"}, {"ТипХДТО.xlsx"}};

    public static void main(String[] args) {
        new ClientGUI();
    }

    private ClientGUI() {

        clientFrame = new JFrame("Client window");
        clientFrame.setSize(500, 500);
        clientFrame.setLocation(500, 100);
        clientFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel loginPanel = new JPanel();

        JPanel selectPanel = new JPanel();

        JTextField ipAdress = new JTextField("IP adress");
        JTextField port = new JTextField("port");

        connectButton = new JButton("Connect");
        connectButton.addActionListener(this);

        registerButton = new JButton("Register");
        registerButton.addActionListener(this);

        selectButton = new JButton("Select file");
        selectButton.addActionListener(this);

        selectedFile = new JTextField(20);

        textArea = new JTextArea();

        loginPanel.add(ipAdress);
        loginPanel.add(port);
        loginPanel.add(login);
        loginPanel.add(password);
        loginPanel.add(connectButton);
        loginPanel.add(registerButton);

        clientFrame.add(loginPanel, BorderLayout.NORTH);
        clientFrame.add(textArea, BorderLayout.CENTER);

        selectPanel.add(selectButton);
        selectPanel.add(selectedFile);
        clientFrame.add(selectPanel, BorderLayout.SOUTH);

        clientFrame.setVisible(true);

//        clientFrame.dispose();

//        try {
//            clientSocket = new Socket("localhost", 8089);
////            oos = new ObjectOutputStream(clientSocket.getOutputStream());
////            oos = new DataOutputStream(clientSocket.getOutputStream());
////            dis = new DataInputStream(clientSocket.getInputStream());
////            oos.writeUTF(Thread.currentThread().getName());
////            oos.flush();
////            while (!clientSocket.isOutputShutdown()) {
////                if (clientCommand != null) {
////                    oos.writeUTF(clientCommand);
////                    oos.flush();
////
////                    System.out.println("Clien sent message " + clientCommand + " to server.");
////                    Thread.sleep(1000);
////
////                    if (clientCommand.equalsIgnoreCase("quit")) {
////                        System.out.println("Client kill connections");
////                        Thread.sleep(2000);
////                        if (ois.read() > -1) {
////                            System.out.println("reading...");
////                            String in = ois.readUTF();
////                            System.out.println(in);
////                        }
////                        break;
////                    }
////                    clientCommand = null;
////                    if (ois.read() > -1) {
////                        System.out.println("reading...");
////                        String in = ois.readUTF();
////                        System.out.println(in);
////                    }
////                }
////            }
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (clientSocket != null) {
            if (e.getSource() == connectButton) {
                sendLoginMessage(false);
            } else if (e.getSource() == registerButton) {
                sendLoginMessage(true);
            } else if (e.getSource() == selectButton) {
                JFileChooser fileChooser = new JFileChooser();
                int ret = fileChooser.showDialog(null, "Select file");
                if (ret == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    selectedFile.setText(file.getName());
                    try {
                        FileMessage fm = new FileMessage(file.getName(), Files.readAllBytes(Paths.get(file.getAbsolutePath())));
//                    oos.reset();
                        ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                        oos.writeObject(fm);
//                    oos.close();
                        oos.flush();
//                    String returnMsg = dis.readUTF();
//                    textArea.append(returnMsg + "\n");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
//            } else if (e.getSource() != connectButton && clientSocket == null) {
//                JDialog dialog = new JDialog();
//                dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
//                dialog.setSize(180, 90);
//                dialog.setVisible(true);
            }

        } else if (e.getSource() == connectButton) {
            try {
                clientSocket = new Socket("localhost", 8089);
                sendLoginMessage(false);
            } catch (IOException e1) {
                e1.printStackTrace();
                textArea.append("Server not found! \n");
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
            String returnMsg = ois.readUTF();
            if (newUser) {
                String findString = "added";

                textArea.append(returnMsg + "\n");
//                    if (returnMsg.contains(findString.subSequence(0, findString.length() - 1))) {
//                        drawTree(login.getText());
//                    }

            } else {
                String findString = "connected";

                textArea.append(returnMsg + "\n");
                File[] userFiles = Network.getUserFileStructure(login.getText());
                if (returnMsg.contains(findString.subSequence(0, findString.length() - 1))) {
                    drawTree(login.getText(), userFiles);
                }
            }

        } catch (IOException e1) {
            e1.printStackTrace();
            textArea.append(e1.getLocalizedMessage() + "\n");
        }
    }

    private void drawTree(String root, File[] files) {
        TreeModel model = createTreeModel(root, files);
        JTree tree1 = new JTree(model);
        TreeSelectionModel selModel = new DefaultTreeSelectionModel();
        selModel.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        // Подключение моделей выделения
        tree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        JPanel contents = new JPanel(new GridLayout(1, 3));
        // Размещение деревьев в интерфейсе
        contents.add(new JScrollPane(tree1));
        clientFrame.getContentPane().add(contents);
        // Размещение текстового поля в нижней части интерфейса
        clientFrame.getContentPane().add(new JScrollPane(textArea), BorderLayout.SOUTH);
    }

    private TreeModel createTreeModel(Object obj, File[] files) {
        // Корневой узел дерева
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(obj);
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
        DefaultMutableTreeNode itemNode = new DefaultMutableTreeNode(item.getName());
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
            if (textArea.getText().length() > 0)
                textArea.append("-----------------------------------\n");
            // Источник события - дерево
            JTree tree = (JTree) e.getSource();
            // Объекты-пути ко всем выделенным узлам дерева
            TreePath[] paths = e.getPaths();
            textArea.append(String.format("Изменений в выделении узлов : %d\n",
                    paths.length));
            // Список выделенных элементов в пути
            TreePath[] selected = tree.getSelectionPaths();
            int[] rows = tree.getSelectionRows();
            // Выделенные узлы
            for (int i = 0; i < selected.length; i++) {
                textArea.append(String.format("Выделен узел : %s (строка %d)\n",
                        selected[i].getLastPathComponent(), rows[i]));
            }
            // Отображение полных путей в дереве для выделенных узлов
            for (int j = 0; j < selected.length; j++) {
                TreePath path = selected[j];
                Object[] nodes = path.getPath();
                String text = "ThreePath : ";
                for (int i = 0; i < nodes.length; i++) {
                    // Путь к выделенному узлу
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes[i];
                    if (i > 0)
                        text += " >> ";
                    text += String.format("(%d) ", i) + node.getUserObject();
                }
                text += "\n";
                textArea.append(text);
            }
        }
    }
}
