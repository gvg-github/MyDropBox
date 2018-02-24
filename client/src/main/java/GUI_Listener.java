import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import static java.awt.event.KeyEvent.KEY_RELEASED;

public class GUI_Listener implements KeyEventDispatcher, ActionListener {

    private ClientGUI clientFrame;

    public GUI_Listener(ClientGUI clientFrame) {
        this.clientFrame = clientFrame;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {

        if (e.getID() == KEY_RELEASED) {

            if (e.getKeyCode() == KeyEvent.VK_DELETE && e.isControlDown()) {
                deleteFolder();
                return true;
            }
            if (e.getKeyCode() == KeyEvent.VK_R && e.isControlDown()) {
                renameFolder();
                return true;
            }

            if (e.getKeyCode() == KeyEvent.VK_C && e.isControlDown()) {
                createFolder();
                return true;
            }

            if (e.getKeyCode() == KeyEvent.VK_S) {
                sendFile();
                return true;
            }
            if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                deleteFile();
                return true;
            }

            if (e.getKeyCode() == KeyEvent.VK_G) {
                getFile();
                return true;
            }

            if (e.getKeyCode() == KeyEvent.VK_I) {
                getFileID();
                return true;
            }

            if (e.getKeyCode() == KeyEvent.VK_A && e.isAltDown()) {
                refreshFile();
                return true;
            }

            if (e.getKeyCode() == KeyEvent.VK_R) {
                renameFile();
                return true;
            }

            if (e.getKeyCode() == KeyEvent.VK_T) {
                transferFile();
                return true;
            }

        }
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        clientFrame.restartTimer();
        if (clientFrame.getClientSocket() != null && !clientFrame.getClientSocket().isClosed()) {
            if (e.getSource() == clientFrame.getGetFileButton()) {
                getFile();

            } else if (e.getSource() == clientFrame.getGetIdButton()) {
                getFileID();

            } else if (e.getSource() == clientFrame.getDeleteFileButton()) {
                deleteFile();

            } else if (e.getSource() == clientFrame.getRenameFileButton()) {
                renameFile();

            } else if (e.getSource() == clientFrame.getTransferFileButton()) {
                transferFile();
            } else if (e.getSource() == clientFrame.getRefreshFileButton()) {
                refreshFile();

            } else if (e.getSource() == clientFrame.getSendFileButton()) {
                sendFile();

            } else if (e.getSource() == clientFrame.getCreateDirButton()) {
                createFolder();
            } else if (e.getSource() == clientFrame.getRenameDirButton()) {
                renameFolder();
            } else if (e.getSource() == clientFrame.getDeleteDirButton()) {
                deleteFolder();
            }
        } else {
            clientFrame.closeClientFrame("Socket closed!");
        }

    }

    private boolean showFileWarningMessage() {
        if (clientFrame.getSelectedPath().length() == 0 || clientFrame.getTecNode() == null || clientFrame.getTecNode().getAllowsChildren()) {
            JOptionPane.showMessageDialog(clientFrame, "File not selected!\n", "", JOptionPane.WARNING_MESSAGE);
            return true;
        }
        return false;
    }

    private boolean showFolderWarningMessage() {
        if (clientFrame.getSelectedPath().length() == 0 || clientFrame.getTecNode() == null || !clientFrame.getTecNode().getAllowsChildren()) {
            JOptionPane.showMessageDialog(null, "Folder not selected!\n", "", JOptionPane.WARNING_MESSAGE);
            return true;
        }
        return false;
    }

    private void sendFile() {
        clientFrame.sendFileMessage(FileActionEnum.SEND, null, null);
    }

    private void deleteFile() {
        if (showFileWarningMessage()) return;

        int res = JOptionPane.showConfirmDialog(null, "Really delete this file?");
        if (res == JOptionPane.NO_OPTION) return;
        clientFrame.sendFileMessage(FileActionEnum.DELETE, null, null);
    }

    private void getFile() {
        if (showFileWarningMessage()) return;
        clientFrame.sendFileMessage(FileActionEnum.GET, null, null);
    }

    private void getFileID() {
        if (showFileWarningMessage()) return;
        clientFrame.sendFileMessage(FileActionEnum.GET_ID, null, null);
    }

    private void renameFile() {
        if (showFileWarningMessage()) return;

        String newFileName = JOptionPane.showInputDialog(clientFrame, "Input file name:");
        if (newFileName != null) {
            clientFrame.sendFileMessage(FileActionEnum.RENAME, newFileName, null);
        }
    }

    private void transferFile() {
        if (showFileWarningMessage()) return;

        StringBuilder tecFolder = clientFrame.getSelectedPath();
        if (clientFrame.getTecNode().isLeaf()) {
            tecFolder = clientFrame.getFolder(clientFrame.getSelectedPath().toString());
        }
        String tecPath = clientFrame.getSelectedPath().toString();
        Object res = JOptionPane.showInputDialog(clientFrame, "Select new folder for file:", "", JOptionPane.QUESTION_MESSAGE, null, (Object[]) clientFrame.getFolderList().toArray(), clientFrame.getFolderList().get(0));
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
        clientFrame.sendFileMessage(FileActionEnum.TRANSFER, newPath, tecPath);
    }

    private void refreshFile() {
        if (showFileWarningMessage()) return;
        String tecPath = clientFrame.getSelectedPath().toString();
        clientFrame.sendFileMessage(FileActionEnum.REFRESH, null, tecPath);
    }

    private void deleteFolder() {
        if (showFolderWarningMessage()) return;
        clientFrame.sendFolderMessage(false, true);
    }

    private void renameFolder() {
        if (showFolderWarningMessage()) return;
        clientFrame.sendFolderMessage(false, false);
    }

    private void createFolder() {
        clientFrame.sendFolderMessage(true, false);
    }

}
