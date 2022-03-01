package dickclock.team.snake;

/**
 * This class is used for manage the snake. All the methods about the snake are here.
 *
 * @author Arnaud Ducrey, Julien Chevalley, Rémi Heredero
 * @date 21/01/22
 */
public class Snake {
    private final XY startXY; // position X of start the snake
    private final int startLength; // first length of the snake

    public int length; // current length of snake

    /**
     * Create a Snake to position startX, startY for the head with the length of startLength
     *
     * @param startX      Position X for the head at the beginning of this Snake
     * @param startY      Position Y for the head at the beginning of this Snake
     * @param startLength Length of the Snake at the beginning of this Snake
     */
    public Snake(XY start, int startLength) {
        this.startXY = start;
        this.startLength = startLength;
        length = startLength;
    }

    /**
     * Create a Snake to position startX, startY for the head with the length of startLength
     *
     * @param startX      Position X for the head at the beginning of this Snake
     * @param startY      Position Y for the head at the beginning of this Snake
     * @param startLength Length of the Snake at the beginning of this Snake
     * @param board       The board of the game for check if the snake isn't to and not at the good place at the beginning
     */
    public Snake(XY start, int startLength, int[][] board) {
        this(
                new XY(checkValue(start.x, board[0].length) ? start.x : board[0].length,
                        checkValue(start.y, board.length) ? start.y : board.length),
                checkValue(startLength, board[0].length) ? startLength : board[0].length
        );
        startLength = checkValue(startLength, board[0].length) ? startLength : board[0].length;
    }

    /**
     * Place the snake on the board
     * Should be used only on the beginning of the game
     *
     * @param board Initial board
     * @return The new board
     */
    public int[][] placeSnake(int[][] board) {
        int s = 1;
        for (int x = startXY.x; s <= startLength; x--) {
            board[startXY.y][x] = s++;
        }

        return board;
    }

    /**
     * Place the snake on the board
     * Should be used only during the game
     *
     * @param board      Initial board
     * @param positionXY position to place head
     * @return The new board
     */
    public int[][] placeSnake(int[][] board, XY positionXY) {
        int headX = positionXY.x;
        int headY = positionXY.y;
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[0].length; x++) {
                if (checkValue(board[y][x], length)) {
                    board[y][x]++;
                } else if (board[y][x] >= length) {
                    board[y][x] = 0;
                }
            }
        }
        board[headY][headX] = 1;
        return board;
    }

    /**
     * Just growUp the Snake with the value in argument
     *
     * @param n For how many the snake should grow up
     */
    public void growUp(int n) {
        length += Math.abs(n);
    }

    /**
     * Just get the length of the snake
     *
     * @return the length of the snake
     */
    public int getLength() {
        return length;
    }

    /**
     * Calculate the score. It's just the difference between the current length and the start length
     *
     * @return Difference of snake during all is life
     */
    public int getScore() {
        return length - startLength;
    }

    /**
     * Just reset the length of snake. For the end of the game
     */
    public void reset() {
        length = startLength;
    }

    /**
     * Check if a value is under a limit but positive
     *
     * @param val   The value to be tested
     * @param limit The limit for the value
     * @return true if the value is under the limit and positive and return false if not
     */
    public static boolean checkValue(int val, int limit) {
        boolean underLimit = val < limit;
        boolean positive = val > 0;
        return underLimit && positive;
    }
}
