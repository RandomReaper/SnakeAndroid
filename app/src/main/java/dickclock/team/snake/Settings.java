package dickclock.team.snake;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Just a class for keep all settings in same place
 */
public class Settings extends MainActivity {
    public enum level {EASY, MEDIUM, HARD}

    // Color for drawing different parts of snake and the background
    private static final String mtcBorder = "borderColor";
    private static final String mtcGrid = "gridColor";
    private static final String mtcHead = "headColor";
    private static final String mtcBody = "bodyColor";
    private static final String mtcApple = "appleColor";
    private static final String mtcBanana = "bananaColor";
    private static final String mtcBackground = "backgroundColor";
    public static int border = Color.MAGENTA;
    public static int grid = Color.BLACK;
    public static int head = Color.BLUE;
    public static int body = Color.BLACK;
    public static int apple = Color.RED;
    public static int banana = Color.YELLOW;
    public static int background = Color.WHITE;

    // User settings
    private static final String mtBorder = "border";
    private static final String mtGrid = "grid";
    private static final String mtSensitivity = "sensitivity2";
    public static boolean hasBorder = true;
    public static boolean hasGrid = true;
    public static int sensitivity = 16;

    // Fun settings
    private static final String mtChanceFruits = "fruitsChance";
    private static final String mtNbrFruits = "numberOfFruits";
    public static final int fruitsChanceBase = 1;
    public static final int numberOfFruitsBase = 1;
    public static int fruitsChance = 1; // With 1, it's only Apple but with 2 is 1/2 Banana and 1/2 apple. with 3 is 1/3 for banana, ...
    public static int numberOfFruits = 1;
    public static boolean funModeOn = false;

    // Debug settings
    public static boolean gravitySensor = false;

    // Konami code settings and method
    public static boolean konami = false;
    private static final Interface.direction[] konamiList = new Interface.direction[8];
    private static int nbrDirOnList = 0;
    private static Interface.direction prevDir = Interface.direction.NOCHANGE;
    public static int[] konamiColor = new int[]{Color.RED, 0xFFFF7F00, Color.YELLOW, Color.GREEN, Color.BLUE, 0xFF4B0082, 0xFF9400D3};
    public static final int konamiSensitivity = 30;
    public static void addKonami(Interface.direction dir){
        if(SettingsFragment.onSettingsView && !konami){
            if (nbrDirOnList >7){
                if (konamiList[0] == Interface.direction.UP &&
                    konamiList[1] == Interface.direction.UP &&
                    konamiList[2] == Interface.direction.DOWN &&
                    konamiList[3] == Interface.direction.DOWN &&
                    konamiList[4] == Interface.direction.LEFT &&
                    konamiList[5] == Interface.direction.RIGHT &&
                    konamiList[6] == Interface.direction.LEFT &&
                    konamiList[7] == Interface.direction.RIGHT){
                    konami = true;
                    Log.i(MainActivity.TAG, "KONAMI!!");
                    Toast.makeText(MainActivity.instance.getBaseContext(), "KONAMI !!", Toast.LENGTH_LONG).show();
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "KONAMI");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "KONAMI");
                    MainActivity.instance.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT,bundle);
                    Settings.head = Color.GREEN;
                } else {
                    nbrDirOnList = 0;
                }
            } else {
                if (dir == Interface.direction.NOCHANGE && dir != prevDir){
                    nbrDirOnList++;
                    prevDir = dir;
                } else if(dir != prevDir){
                    konamiList[nbrDirOnList] = dir;
                    prevDir = dir;
                }
            }
        } else {
            nbrDirOnList = 0;
        }
    }

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

    public static void getSettingsFromMemorize(){
        hasBorder = getFromMemorize(mtBorder, hasBorder);
        hasGrid = getFromMemorize(mtGrid, hasGrid);
        sensitivity = getFromMemorize(mtSensitivity, sensitivity);
        fruitsChance = getFromMemorize(mtChanceFruits, fruitsChance);
        numberOfFruits = getFromMemorize(mtNbrFruits, numberOfFruits);
        border = getFromMemorize(mtcBorder, border);
        grid = getFromMemorize(mtcGrid, grid);
        head = getFromMemorize(mtcHead, head);
        body = getFromMemorize(mtcBody, body);
        apple = getFromMemorize(mtcApple, apple);
        banana = getFromMemorize(mtcBanana, banana);
        background = getFromMemorize(mtcBackground, background);
    }
    public static void pushSettingsOnMemorize(){
        MainActivity.editorSettings.putBoolean(mtBorder, hasBorder);
        MainActivity.editorSettings.putBoolean(mtGrid, hasGrid);
        MainActivity.editorSettings.putInt(mtSensitivity, sensitivity);
        MainActivity.editorSettings.putInt(mtChanceFruits, fruitsChance);
        MainActivity.editorSettings.putInt(mtNbrFruits, numberOfFruits);
        MainActivity.editorSettings.putInt(mtcBorder, border);
        MainActivity.editorSettings.putInt(mtcGrid, grid);
        MainActivity.editorSettings.putInt(mtcHead, head);
        MainActivity.editorSettings.putInt(mtcBody, body);
        MainActivity.editorSettings.putInt(mtcApple, apple);
        MainActivity.editorSettings.putInt(mtcBanana, banana);
        MainActivity.editorSettings.putInt(mtcBackground, background);
        MainActivity.editorSettings.commit();
    }


    private static int getFromMemorize(String key, int def){ return (MainActivity.settings.getInt(key, def)); }
    private static double getFromMemorize(String key, double def){ return (MainActivity.settings.getFloat(key, (float)def)); }
    private static boolean getFromMemorize(String key, boolean def){ return (MainActivity.settings.getBoolean(key, def)); }


}
