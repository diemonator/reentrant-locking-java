/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package movingballsfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class MovingBallsFX extends Application {
    
    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    private Thread threadDraw;
    private final Ball[] ballArray = new Ball[10];
    private final Thread[] threadArray = new Thread[10];
    private final CheckBox[] checkBoxArray = new CheckBox[10];
    private final Circle[] circleArray = new Circle[10];
    private final int minX = 100;
    private final int maxX = 700;
    private final int maxY = 400;
    private final int radius = 10;
    private final int minCsX = (maxX + minX) / 2 - 100;
    private final int maxCsX = (maxX + minX) / 2 + 100;

    private ReaderWriterMonitor monitor;
    
    @Override
    public void start(Stage primaryStage) {

        // Create the scene
        Group root = new Group();
        Scene scene = new Scene(root, maxX, maxY+(4*radius));
        monitor = new ReaderWriterMonitor();
        // Check boxes
        for (int i = 0; i < checkBoxArray.length; i++) {
            String text;
            if (i < 5) text = "Reader-"+(i+1);
            else text = "Writer-"+(i-4);
            final int index = i;
            checkBoxArray[i] = new CheckBox(text);
            checkBoxArray[i].addEventHandler(MouseEvent.MOUSE_CLICKED,
                    event -> checkBoxMouseClicked(event,index));
            checkBoxArray[i].setLayoutX(radius);
            checkBoxArray[i].setLayoutY((i*4 + 1)*radius);
            root.getChildren().add(checkBoxArray[i]);
        }
        CheckBox readerPriority = new CheckBox("writers have priority");
        readerPriority.setLayoutX(radius);
        readerPriority.setLayoutY((checkBoxArray.length*4+1)*radius);
        readerPriority.addEventHandler(MouseEvent.MOUSE_CLICKED,
                event -> monitor.setWriterPriorityOn());
        root.getChildren().add(readerPriority);
        readerPriority = new CheckBox("readers have priority");
        readerPriority.setLayoutX(radius*radius+100);
        readerPriority.setLayoutY((checkBoxArray.length*4+1)*radius);
        readerPriority.addEventHandler(MouseEvent.MOUSE_CLICKED,
                event -> monitor.setReaderPriorityOn());
        root.getChildren().add(readerPriority);
        
        // Indicate entire section
        Rectangle entireSection = new Rectangle(minX,0,maxX-minX,maxY);
        entireSection.setFill(Color.LIGHTYELLOW);
        root.getChildren().add(entireSection);
        
        // Indicate critical section
        Rectangle criticalSection = new Rectangle(minCsX,0,maxCsX-minCsX,maxY);
        criticalSection.setFill(Color.LIGHTGREEN);
        root.getChildren().add(criticalSection);
        
        // Define circles for each ball
        for (int i = 0; i < circleArray.length; i++) {
            CheckBox cb = checkBoxArray[i];
            int y = (int) cb.getLayoutY() + radius;
            if (i < 5) {
                // Reader
                circleArray[i] = new Circle(minX, y, radius, Color.RED);
            }
            else {
                // Writer
                circleArray[i] = new Circle(minX, y, radius, Color.BLUE);
            }
            circleArray[i].setVisible(false);
            root.getChildren().add(circleArray[i]);
        }
        
        // Define title and assign the scene for main window
        primaryStage.setTitle("Moving Balls");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Start thread to draw each 20 ms
        threadDraw = new Thread(new DrawRunnable());
        threadDraw.start();

        new ThreadOverview();
    }
    
    private void checkBoxMouseClicked(MouseEvent event, int index) {
        CheckBox cb = checkBoxArray[index];
        int y = (int) cb.getLayoutY() + radius;
        if (cb.isSelected() && index < 5) { 
            // Reader selected: new red ball
            Ball b = new Ball(minX, maxX, minCsX, maxCsX, y, Color.RED);
            ballArray[index] = b;
            Thread t = new Thread(new BallRunnable(b, monitor),"Reader "+(++index));
            index--;
            threadArray[index] = t;
            circleArray[index].setVisible(true);
            t.start();
        } else if (cb.isSelected() && index >= 5) { 
            // Writer selected: new blue ball
            Ball b = new Ball(minX, maxX, minCsX, maxCsX, y, Color.BLUE);
            ballArray[index] = b;
            index -= 4;
            Thread t = new Thread(new BallRunnable(b, monitor), "Writer "+(index));
            index += 4;
            threadArray[index] = t;
            circleArray[index].setVisible(true);
            t.start();
        } else {
            // Reader or writer deselected: remove ball
            threadArray[index].interrupt();
            threadArray[index] = null;
            ballArray[index] = null;
            circleArray[index].setVisible(false);
            circleArray[index].setCenterX(minX);
        }
    }
    
    private void updateCircles() {
        for (int i = 0; i < ballArray.length; i++) {
            Ball b = ballArray[i];
            Circle c = circleArray[i];
            if (b != null) {
                c.setCenterX(b.getXPos());
                c.setCenterY(b.getYPos());
            }
        }
    }

    @Override
    public void stop() {
        threadDraw.interrupt();
        for (Thread t : threadArray) {
            if (t != null) t.interrupt();
        }
    }
    private class DrawRunnable implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(20);
                    Platform.runLater(MovingBallsFX.this::updateCircles);
                }
            } catch (InterruptedException ignored) { }
        }
    }
}
