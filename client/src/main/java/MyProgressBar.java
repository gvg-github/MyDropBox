import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class MyProgressBar extends Thread {

    private final JFrame frame = new JFrame();
    private final JDialog dialog = new JDialog(frame, "Transfer in process...", false);
    private final JProgressBar bar = new JProgressBar();
    private final JLabel label = new JLabel("Task in process...");
    private boolean showBar;

    public MyProgressBar() {

        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(0);

//        dialog.getContentPane().add(bar);
//        label.setVisible(true);
//        bar.setVisible(true);
        bar.setIndeterminate(true);
        bar.setStringPainted(true);
        bar.setString("Task in process...");
        label.setSize(300, 20);
        dialog.setSize(400, 50);
        JPanel panel = new JPanel(new GridLayout(2, 0));
        panel.setVisible(true);
        panel.add(label);
        panel.add(bar);
        dialog.getContentPane().add(panel);
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
        dialog.setLocation((int) (max_width - dialog.getWidth()) / 2, (int) (max_height - dialog.getHeight()) / 2);
//        dialog.setLocation((int) (max_width - dialog.getWidth()) / 4, (int) (max_height - dialog.getHeight()) / 2);
//        start();

    }

    public void run() {

//        dialog.setAlwaysOnTop(false);
//        label.setVisible(true);
//        while(true){
//            if (showBar){
//
//                dialog.setVisible(true);
//                System.out.println("PBar show...");
//
//            }else{
//                dialog.setVisible(false);
//                System.out.println("PBar hide...");
//
//            }
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        }


    }

    public void showBar() {
        showBar = true;
        dialog.setVisible(true);
        label.setVisible(true);
        bar.setVisible(true);
        System.out.println("PBar show...");
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void hideBar() {
        showBar = false;
        dialog.setVisible(false);
        System.out.println("PBar hide...");
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
