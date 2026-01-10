package GUI;

import javax.swing.*;
import java.awt.*;

public class GameTimer {
    private final JLabel timeLabel;
    private final Timer timer;
    private long startTime;
    private long endTime;
    private boolean isRunning;

    public GameTimer(JLabel timeLabel) {
        this.timeLabel = timeLabel;
        this.isRunning = false;

        // Timer visuals
        timeLabel.setText("Time: 0s");
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Updating timer every 100 ms
        this.timer = new Timer(100, e -> updateTimer());
    }

    // Starting the timer
    public void start() {
        if (!isRunning) {
            startTime = System.currentTimeMillis();
            timer.start();
            isRunning = true;
        }
    }

    // Stopping the timer
    public void stop() {
        if (isRunning) {
            endTime = System.currentTimeMillis();
            timer.stop();
            isRunning = false;
        }
    }

    public void reset() {
        if (isRunning) {
            timer.stop();
            isRunning = false;
        }
        timeLabel.setText("Time: 0s");
    }

    // Calculating deltaTime
    private void updateTimer() {
        long currentTime = System.currentTimeMillis();
        long elapsedMillis = currentTime - startTime;
        long seconds = elapsedMillis / 1000;
        timeLabel.setText("Time: " + seconds + "s");
    }

    public int getTimeInSeconds() {
        long end = isRunning ? System.currentTimeMillis() : endTime;
        return (int) ((end - startTime) / 1000);
    }
}
