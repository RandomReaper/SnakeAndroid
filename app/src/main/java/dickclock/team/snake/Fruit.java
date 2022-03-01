package dickclock.team.snake;

import java.util.Random;

public class Fruit {

    public final static int VALUEFORAPPLE = -1;
    public final static int VALUEFORBANANA = -2;
    private int numberOfFruit = 0;

    /**
     * Create and place an Apple on the board game
     * @param board The initial board game
     * @return The new board game
     */
    public int[][] createFruit(int[][] board){
        int randomX;
        int randomY;
        do {
            randomX = new Random().nextInt(board[0].length); // generate an number between 0 and the width of the board
            randomY = new Random().nextInt(board.length); // generate an number between 0 and the height of the board
        } while (board[randomY][randomX] != 0);
        board[randomY][randomX] = chooseFruit();
        numberOfFruit++;
        return board;
    }

    /**
     * Check if the value is a fruit
     * @param f The value to be tested
     * @return true if the value is a fruit and false if is not
     */
    public boolean isFruit(int f){
        return f < 0;
    }

    /**
     * Return the value of the fruit (should used with grow up method on snake)
     * @param f the value of the fruit (the real value on the board)
     * @return the positive value of this fruit
     */
    public int eatFruit(int f){
        numberOfFruit--;
        return Math.abs(f);
    }

    /**
     * Get the number of fruit on the board
     * @return the number of fruit on the board
     */
    public int getNumberOfFruit(){
        return numberOfFruit;
    }

    /**
     * Just reset the number of fruit. For the end of the game
     */
    public void reset() {
        numberOfFruit = 0;
    }

    /**
     * Generate a random fruit
     * @return return the random fruit
     */
    private int chooseFruit(){
        switch (new Random().nextInt(Settings.fruitsChance)) {
            case 1:
                return VALUEFORBANANA;

            default:
                return VALUEFORAPPLE;
        }
    }
}
