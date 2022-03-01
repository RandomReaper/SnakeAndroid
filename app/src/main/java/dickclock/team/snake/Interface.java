package dickclock.team.snake;

import android.util.Log;

/**
 * Class for manage interface
 * Get next direction with sensors and print on Terminal some information
 * @author Arnaud Ducrey, Julien Chevalley, Rémi Heredero
 */
public class Interface {

    // Define a new type for direction
    public enum direction {
        UP,
        RIGHT,
        DOWN,
        LEFT,
        NOCHANGE
    }

    public direction nextDir; // Define nextDirection

    /**
     * Get next direction with orientation on phone
     * @param x degrees in roll axis
     * @param y degrees in pitch axis
     * @return next direction
     */
    public direction getNextDir(int x, int y){
        int limit = (SettingsFragment.onSettingsView) ? Settings.konamiSensitivity : Settings.sensitivity;
        nextDir = direction.NOCHANGE;

        boolean xInLimit = x > - limit && x < limit;
        boolean yInLimit = y > - limit && y < limit;

        boolean b = x > limit && ( yInLimit || x > Math.abs(y) );
        boolean b1 = x < -limit && ( yInLimit || x < -Math.abs(y) );
        boolean b2 = y > limit && ( xInLimit || y < Math.abs(x) );
        boolean b3 = y < -limit && ( xInLimit || y < -Math.abs(x) );

        /* To type of sensor can be used, depend witch phone
         * Both are different for calculate the angle
         * So, depend the sensor, next direction by the angle isn't same
         */
        if(Settings.gravitySensor){
            if(b){ nextDir = direction.LEFT; }
            if(b1){ nextDir = direction.RIGHT; }
            if(b2){ nextDir = direction.DOWN; }
            if(b3){ nextDir = direction.UP; }
        } else {
            if(b){ nextDir = direction.RIGHT; }
            if(b1){ nextDir = direction.LEFT; }
            if(b2){ nextDir = direction.UP; }
            if(b3){ nextDir = direction.DOWN; }
        }
        return nextDir;
    }

    /**
     * Put the score on terminal (should be used at the end of the game)
     * @param score The score to be put on terminal
     */
    public void putScoreTerminal(int score) {
        Log.i(MainActivity.TAG,"Your score is: " + score);
    }

    /**
     * Draw the board of the game on terminal
     * @param board the current board to be drawing
     */
    public void drawOnTerminal(int[][] board) {
        String s = "Board: \n";
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[0].length; x++) {
                s += board[y][x] + " ";
            }
            s += "\n";
        }
        Log.i(MainActivity.TAG,s);
    }

}