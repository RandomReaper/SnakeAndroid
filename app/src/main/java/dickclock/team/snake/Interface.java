package dickclock.team.snake;

import android.util.Log;

public class Interface {


    // Define a new type for direction
    public enum direction {
        UP,
        RIGHT,
        DOWN,
        LEFT,
        NOCHANGE
    }

    public static direction nextDir; // Define nextDirection
    public static direction prevDir; // Define nextDirection



    public static direction getNextDir(float x, float y){
        double limit = Settings.sensitivity;
        Log.i(MainActivity.TAG, "prevDire: " + prevDir);
        boolean prevUpDown = (prevDir == direction.UP || prevDir == direction.DOWN);
        boolean prevLeftRight = (prevDir == direction.LEFT || prevDir == direction.RIGHT);
        nextDir = direction.NOCHANGE;
        //Log.i(MainActivity.TAG, "xy\n" + x + "\n" + y);
        boolean xInLimit = x > - limit && x < limit;
        boolean yInLimit = y > - limit && y < limit;
        if(Settings.isStair){
            if( x > limit && !prevLeftRight ){ nextDir = direction.LEFT; }
            if( x < -limit && !prevLeftRight ){ nextDir = direction.RIGHT; }
            if( y > limit && !prevUpDown ){ nextDir = direction.DOWN; }
            if( y < -limit && !prevUpDown ){ nextDir = direction.UP; }
        } else {
            if( x > limit && yInLimit ){ nextDir = direction.LEFT; }
            if( x < -limit && yInLimit ){ nextDir = direction.RIGHT; }
            if( y > limit && xInLimit ){ nextDir = direction.DOWN; }
            if( y < -limit && xInLimit ){ nextDir = direction.UP; }
        }
        prevDir = nextDir;
        return nextDir;
    }




    /**
     * Put the score on terminal (should be used at the end of the game)
     * @param score The score to be put on terminal
     */
    public static void putScoreTerminal(int score) {
        Log.i(MainActivity.TAG,"Your score is: " + score);
    }

    /**
     * Draw the board of the game on terminal
     * @param board the current board to be drawing
     */
    public static void drawOnTerminal(int[][] board) {
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