import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class MyProgressBar extends Thread {

    private final JFrame frame = new JFrame();
    private final JDialog dialog = new JDialog(frame, "Выполнение задачи...   ", false);
    private final JProgressBar progressBar = new JProgressBar();
    private boolean showBar;

    /**
     * Конструктор progreeBar и диалога, в котором он содержится
     */

    public MyProgressBar() {

        showBar = false;
        frame.setUndecorated(true);
        progressBar.setIndeterminate(true);
        progressBar.setString("File transferred...");
        dialog.setUndecorated(true);

        dialog.getContentPane().add(new JLabel("File transferred..."));
        dialog.getContentPane().add(progressBar);
        dialog.pack();

        dialog.setDefaultCloseOperation(0);
        // Задаем центровку диалога.
        Toolkit kit = dialog.getToolkit();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        Insets in = kit.getScreenInsets(gs[0].getDefaultConfiguration());
        Dimension d = kit.getScreenSize();
        int max_width = (d.width - in.left - in.right);
        int max_height = (d.height - in.top - in.bottom);
//        dialog.setLocation((int) (max_width - dialog.getWidth()) / 2, (int) (max_height - dialog.getHeight()) / 2);
        dialog.setLocation((int) (max_width - dialog.getWidth()) / 3, (int) (max_height - dialog.getHeight())/3);
        start();
    }

    @Override
    public void run() {
        while (true) {
            try {
//                dialog.setVisible(true);
//                progressBar.setVisible(true);
//                dialog.setAlwaysOnTop(true);
                if (showBar) {
                    dialog.setVisible(true);
                    progressBar.setVisible(true);
                    dialog.setAlwaysOnTop(true);
                } else {
                    if (dialog.isVisible()) {
                        dialog.setVisible(false);
                    }
                }
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void setShowBar(boolean showBar) {
        this.showBar = showBar;
    }

    //    /**
//     * Метод, отображающий диалог
//     */
//    public void showDialog() {
//        dialog.setVisible(true);
//        progressBar.setVisible(true);
//        dialog.setAlwaysOnTop(true);
//    }
//
//    /**
//     * Метод, закрывающий диалог
//     */
//    public void closeDialog() {
//        if (dialog.isVisible()) {
////            dialog.getContentPane().remove(progressBar);
////            dialog.getContentPane().validate();
//            dialog.setVisible(false);
//
//        }
//    }
}

