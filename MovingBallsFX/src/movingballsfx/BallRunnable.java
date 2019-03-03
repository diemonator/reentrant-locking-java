/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package movingballsfx;

import javafx.scene.paint.Color;

public class BallRunnable implements Runnable {

    private final Ball ball;
    private ReaderWriterMonitor m;

    public BallRunnable(Ball ball, ReaderWriterMonitor m) {
        this.ball = ball;
        this.m = m;
    }

    private void busySleep(long ms)
    {
        long end = System.currentTimeMillis() + ms;
        
        while (System.currentTimeMillis() < end)
        {
            // busy waiting...
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            ball.move();
            busySleep(ball.getSpeed());
            if (ball.isEnteringCs()) {
                if (ball.getColor() == Color.RED) {
                    m.enterReader();
                    continue;
                }
                m.enterWriter();
            } else if (ball.isLeavingCs()) {
                if (ball.getColor() == Color.RED) {
                    m.exitReader();
                    continue;
                }
                m.exitWriter();
            }
        }
        if (ball.isInCs()) {
            if (ball.getColor() == Color.RED)
                m.exitReader();
            else m.exitWriter();
        }
    }

}
