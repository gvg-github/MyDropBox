import javax.swing.*;
import java.awt.*;

public class MyProgressBar_Simple {

    private final JFrame frame = new JFrame();
    private final JDialog dialog = new JDialog(frame, "Transfer in process...", false);
    private final JLabel label = new JLabel("Task in process...");
    private final JPanel panel = new JPanel(new GridLayout(1, 0));

    public MyProgressBar_Simple() {

        Font otherFont = new Font("TimesRoman", Font.BOLD, 20);
        frame.setUndecorated(true);
        dialog.setUndecorated(true);

        label.setVisible(true);
        label.setFont(otherFont);
        dialog.setSize(300, 60);
        panel.setVisible(true);
        panel.setSize(200, 40);
        panel.add(label);
        dialog.add(panel);
        dialog.pack();

        dialog.setDefaultCloseOperation(1);

        // Задаем центровку диалога.
        int[] coords = ClientStartFrame.getStartCoords(frame);
        dialog.setLocation((int) (coords[0]- dialog.getWidth()) / 2, (int) (coords[1] - dialog.getHeight()) / 2);
//        Toolkit kit = dialog.getToolkit();
//        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//        GraphicsDevice[] gs = ge.getScreenDevices();
//        Insets in = kit.getScreenInsets(gs[0].getDefaultConfiguration());
//        Dimension d = kit.getScreenSize();
//        int max_width = (d.width - in.left - in.right);
//        int max_height = (d.height - in.top - in.bottom);
//        dialog.setLocation((int) (max_width - dialog.getWidth()) / 2, (int) (max_height - dialog.getHeight()) / 2);

    }

    public void showBar() {
        dialog.setVisible(true);
//        System.out.println("PBar show...");
    }

    public void hideBar() {
        dialog.setVisible(false);
//        System.out.println("PBar hide...");
    }

}
