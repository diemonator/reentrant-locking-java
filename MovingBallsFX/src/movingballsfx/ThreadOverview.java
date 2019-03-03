package movingballsfx;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Joris
 */
public class ThreadOverview implements Runnable {

    private final Label labelInfo;

    public ThreadOverview() {
        Label labelHeader = new Label(" id prio  name                   state      #blocked   cpu(ms)");
        labelInfo = new Label("");
        
        // if you don't get a nice layout with nice aligned columns, change the font name
        // into another font which is available on your system, example: "Courier New"
        labelHeader.setFont(Font.font("DejaVu Sans Mono", 12));
        labelInfo.setFont(Font.font("DejaVu Sans Mono", 12));
        
        VBox root2 = new VBox();
        root2.getChildren().add(labelHeader);
        root2.getChildren().add(labelInfo);
        Scene scene2 = new Scene(root2, 500, 240);
        Stage secondStage = new Stage();
        secondStage.setX(100);
        secondStage.setY(10);
        secondStage.initStyle(StageStyle.UTILITY);
        secondStage.setTitle("thread overview");
        secondStage.setScene(scene2);
        secondStage.show();
        
        Thread t = new Thread (null, this, "Thread-Overview");
        t.start();
    }

    private int getPrio(Thread[] threads, long id)
    {
        for (Thread t : threads)
        {
            try {
                if (t.getId() == id) {
                    return (t.getPriority());
                }
            } catch (NullPointerException e) {
                // thread might have been finished....
                return (-1);
            }
        }
        return (-1);
    }
    
    private void updateThreadInfo() {
        Thread[] threads = new Thread[Thread.activeCount()];
        StringBuilder s = new StringBuilder();
        ThreadMXBean tb = ManagementFactory.getThreadMXBean();
        ThreadInfo[] til = tb.dumpAllThreads(false, false);
        
        Thread.enumerate(threads);
        
        for (ThreadInfo ti : til) {
            String format = "%3d %2d %-25s %-13s %5d %9.3f\n";
            s.append(String.format(format,
                    ti.getThreadId(),
                    getPrio(threads, ti.getThreadId()),
                    ti.getThreadName(),
                    ti.getThreadState().toString(),
                    ti.getBlockedCount(),
                    (float) tb.getThreadCpuTime(ti.getThreadId()) / 1000000.));
        }
        labelInfo.setText(s.toString());

    }

    @Override
    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        try {
            while (true) {
                Thread.sleep(200);
                Platform.runLater(this::updateThreadInfo);
            }
        } catch (InterruptedException ignored) { }
    }
}
