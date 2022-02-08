package dickclock.team.snake;

/**
 * Main Class for the game.
 * Manage everything about the game
 * @date 21/01/22
 * @author Arnaud Ducrey, Julien Chevalley, Rémi Heredero
 */
public class Game {

    final int NUMBEROFFRUIT = Settings.numberOfFruits; // number of fruit we want have on the game in same time

    private final int height;
    private final int width;
    private boolean inProgress; // if the game is on progress or is finish
    private int[][] board; // the current board of game
    public Snake snake; // the snake for the game
    private Interface.direction previousDirection = Interface.direction.RIGHT; // previous direction

    /**
     * Define size for board and create interface
     *
     * @param height height of the board
     * @param width  width of the board
     */
    public Game(int height, int width) {
        this.height = height;
        this.width = width;

    }

    /**
     * Initialize the game.
     * Create the board with the specific size and include the snake
     */
    public void Initialization(){

        // define the game is on progress
        this.inProgress = true;

        // create a board for the game
        this.board = new int[this.height][this.width];

        // Initialization the board with only 0
        for (int y = 0; y < this.board.length; y++) {
            for (int x = 0; x < this.board[0].length; x++) {
                this.board[y][x] = 0;
            }
        }

        // create and place the snake in the board
        this.snake = new Snake(3, this.height / 2, 3, board);
        this.board = this.snake.placeSnake(board);
    }

    /**
     * Check if the game is on progress
     *
     * @return Return true if the game is on progress and false if is not.
     */
    public boolean isInProgress() {
        return this.inProgress;
    }

    /**
     * Main method of Game. All things for play one round (one movement)
     *
     * @param dir Direction the snake should go
     */
    public void play(Interface.direction dir) {

        // If direction didn't change, just continue with same direction than previous
        // round.
        if (dir == Interface.direction.NOCHANGE) {
            dir = previousDirection;
        } else if ((previousDirection == Interface.direction.DOWN && dir == Interface.direction.UP)
                || (previousDirection == Interface.direction.UP && dir == Interface.direction.DOWN)
                || (previousDirection == Interface.direction.RIGHT && dir == Interface.direction.LEFT)
                || (previousDirection == Interface.direction.LEFT && dir == Interface.direction.RIGHT)) {
            dir = previousDirection;
        } else
            previousDirection = dir;

        // Check where is the head of snake at the beginning of the round
        int headX = snake.getHead(board)[0];
        int headY = snake.getHead(board)[1];

        // Change place of head for next round depend to the direction.
        if (dir == Interface.direction.UP) {
            headY--;
        }
        if (dir == Interface.direction.LEFT) {
            headX--;
        }
        if (dir == Interface.direction.DOWN) {
            headY++;
        }
        if (dir == Interface.direction.RIGHT) {
            headX++;
        }

        // Check if head is on the board and if not, continue the game
        if (headY < board.length && headX < board[0].length && headX >= 0 && headY >= 0) {

            // Check if head don't hit a part of body of snake and turn of the game if yes
            if (board[headY][headX] > 0) {
                this.inProgress = false;
            } else {

                // Check if a fruit is on place to the next position of the head of snake and
                // makes the snake grow if yes
                if (Fruit.isFruit(board[headY][headX])) {
                    snake.growUp(Fruit.eatFruit(board[headY][headX]));
                }

                // Place the new snake on the board
                board = snake.placeSnake(board, returnPositionXY(headX, headY));

                // Create a Fruit if not enough fruit(s) exist
                if (Fruit.getNumberOfFruit() < NUMBEROFFRUIT) {
                    board = Fruit.createFruit(board);
                }
            }
        } else {
            this.inProgress = false;
        }

        // if game is finish, reset the number of fruit and the direction
        if(!this.inProgress){
            Fruit.reset();
            Interface.nextDir = Interface.direction.NOCHANGE;
            previousDirection = Interface.direction.RIGHT;
        }
    }

    /**
     * Get the score (should used only at the end of the game)
     * @return score (how many point you gain during the game with eating fruits)
     */
    public int getScore() {
        return snake.getScore();
    }

    /**
     * Get the board of game
     *
     * @return the current board of the game
     */
    public int[][] getBoard() {
        return board;
    }

    /**
     * Get position of a value in current board
     *
     * @param board the current board of game
     * @param val   value to be tested
     * @return the position of the value [0] is X and [1] is Y.
     *         return null if the value isn't found
     */
    public static int[] positionInBoard(int[][] board, int val) {
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[0].length; x++) {
                if (board[y][x] == val) {
                    return returnPositionXY(x, y);
                }
            }
        }
        return null;
    }

    /**
     * returns positionXY in the form in[2] with the integers X and Y as parameters
     *
     * @param posX integer for position in X
     * @param posY integer for position in Y
     * @return the position of the value [0] is X and [1] is Y.
     */
    public static int[] returnPositionXY(int posX, int posY) {
        int[] positionXY = new int[2];
        positionXY[0] = posX;
        positionXY[1] = posY;
        return positionXY;
    }
}
