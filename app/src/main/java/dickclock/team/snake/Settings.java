package dickclock.team.snake;

import android.graphics.Color;

/**
 * Just a class for keep all settings in same place
 */
public class Settings {
    public enum level {EASY, MEDIUM, HARD}

    // Color for drawing different parts of snake and the background
    public static int border = Color.MAGENTA;
    public static int grid = Color.BLACK;
    public static int head = Color.BLUE;
    public static int body = Color.BLACK;
    public static int apple = Color.RED;
    public static int banana = Color.YELLOW;
    public static int background = Color.WHITE;

    // Choice for border and grid
    public static boolean hasBorder = true;
    public static boolean hasGrid = true;

    // Fun settings
    public static double sensitivity = 1.75;
    public static int fruitsChance = 1; // With 1, it's only Apple but with 2 is 1/2 Banana and 2/3 apple. with 3 is 1/3 for banana, ...
    public static int numberOfFruits = 1;

    // Debug settings
    public static boolean isStair = false;
    public static int nbrForCheatCode = 10;

    // Time and size settings + get method
    private static final int timeBasis = 5000;
    private static final int timeEasy = 1;
    private static final int timeMedium = 2;
    private static final int timeHard = 3;

    private static final int sizeEasy = 15;
    private static final int sizeMedium = 20;
    private static final int sizeHard = 25;

    public static int getTime(level l){
        switch (l){
            case EASY:
                return (timeBasis/sizeEasy)/timeEasy;
            case MEDIUM:
                return (timeBasis/sizeMedium)/timeMedium;
            case HARD:
                return (timeBasis/sizeHard)/timeHard;
        }
        return 250;
    }
    public static int getSize(level l){
        switch (l){
            case EASY:
                return sizeEasy;
            case MEDIUM:
                return sizeMedium;
            case HARD:
                return sizeHard;
        }
        return 12;
    }

}
