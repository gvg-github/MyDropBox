import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

class TimerLabel extends JLabel {

    private ClientGUI frame;

//    public TimerLabel(Timer timer, ClientGUI frame) {
//        this.frame = frame;
//        timer.scheduleAtFixedRate(timerTask, 0, 1000);
//    }
//
//    public void restartTimer(Timer timer) {
//        timerTask.cancel();
//    }
//
//    private TimerTask timerTask = new TimerTask() {
//        //        private volatile int time = 601;
//        private volatile int time = 61;
//
//        private Runnable refresher = new Runnable() {
//            @Override
//            public void run() {
//                int t = time;
//                TimerLabel.this.setText(String.format("%02d:%02d", t / 60, t % 60));
//            }
//        };
//
//        @Override
//        public void run() {
//            time--;
//            if (time == 0) frame.closeClientFrame("Closed by timeout...");
//            SwingUtilities.invokeLater(refresher);
//        }
//    };
//
//

    private Timer timer = new Timer();
    private TimerTask timerTask;

    public TimerLabel(ClientGUI frame)

    {
        this.frame = frame;
        restartTimer();
    }

    public void restartTimer() {
        stopTimer();
        timerTask = new TimerTask() {
//            private volatile int time = 61;
            private volatile int time = 9999961;

            @Override
            public void run() {
                time--;
                if (time == 0) frame.closeClientFrame("Closed by timeout...");
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        int t = time;
                        TimerLabel.this.setText(String.format("%02d:%02d", t / 60, t % 60));
                    }
                });
            }
        };
        timer.schedule(timerTask, 0, 1000);
    }

    public void stopTimer() {
        if (timerTask != null)
            timerTask.cancel();
    }
}

