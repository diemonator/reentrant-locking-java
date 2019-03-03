package movingballsfx;

import java.util.Random;
import javafx.scene.paint.Color;

public class Ball {

    private int xPos;
    private final int yPos;
    private final int speed;
    private final int minX, maxX;
    private final Color color;
    private final int minCsX, maxCsX;

    public Ball(int minX, int maxX, int minCsX, int maxCsX, int yPos, Color color) {
        this.xPos = minX;
        this.yPos = yPos;
        this.minX = minX;
        this.maxX = maxX;
        this.minCsX = minCsX;
        this.maxCsX = maxCsX;
        this.speed = 10 + (new Random()).nextInt(5);
        this.color = color;
    }

    public void move() {
        xPos++;
        if (xPos > maxX) xPos = minX;
    }

    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }

    public Color getColor() {
        return color;
    }

    public int getSpeed() {
        return speed;
    }

    public boolean isEnteringCs() {
        return xPos == minCsX;
    }

    public boolean isInCs() {
        return xPos > minCsX && xPos < maxCsX;
    }
    
    public boolean isLeavingCs() {
        return xPos == maxCsX;
    }
}
