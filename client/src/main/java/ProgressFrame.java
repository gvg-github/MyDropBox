import javax.swing.*;
import java.awt.*;

public class ProgressFrame extends JFrame {

    public ProgressFrame() {

        Font otherFont = new Font("TimesRoman", Font.BOLD, 20);

        setSize(new Dimension(300, 60));
        JLabel progressLabel = new JLabel("Task in process...");
        progressLabel.setFont(otherFont);
        progressLabel.setBorder(BorderFactory.createBevelBorder(2));
        add(progressLabel, BorderLayout.NORTH);
        setUndecorated(true);

        // Задаем центровку.
        Toolkit kit = getToolkit();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        Insets in = kit.getScreenInsets(gs[0].getDefaultConfiguration());
        Dimension d = kit.getScreenSize();
        int max_width = (d.width - in.left - in.right);
        int max_height = (d.height - in.top - in.bottom);
//        dialog.setLocation((int) (max_width - dialog.getWidth()) / 2, (int) (max_height - dialog.getHeight()) / 2);
        setLocation((int) ((max_width - getWidth())*10) / 25, (int) (max_height - getHeight()) / 2);
        setAlwaysOnTop(false);
        pack();
        setVisible(true);
    }

    public void changeVisible(boolean visible){
        setVisible(visible);
    }

}

