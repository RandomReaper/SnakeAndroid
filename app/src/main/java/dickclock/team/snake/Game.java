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
    private Snake snake; // the snake for the game
    private Fruit fruit;
    private Interface.direction previousDirection = Interface.direction.RIGHT; // previous direction

    /**
     * Define size for board and create interface
     *
     * @param h height of the board
     * @param w  width of the board
     */
    public Game(int h, int w) {
        height = h;
        width = w;
    }

    /**
     * Initialize the game.
     * Create the board with the specific size and include the snake
     */
    public void Initialization(){

        // define the game is on progress
        inProgress = true;

        // create a board for the game
        board = new int[height][width];

        // Initialization the board with only 0
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[0].length; x++) {
                board[y][x] = 0;
            }
        }

        // create and place the snake in the board
        snake = new Snake(new XY(3, height / 2), 3, board);
        board = snake.placeSnake(board);
    }

    /**
     * Check if the game is on progress
     *
     * @return Return true if the game is on progress and false if is not.
     */
    public boolean isInProgress() {
        return inProgress;
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
        XY head = positionInBoard(1);

        // Change place of head for next round depend to the direction.
        if (dir == Interface.direction.UP) {
            head.y--;
        }
        if (dir == Interface.direction.LEFT) {
            head.x--;
        }
        if (dir == Interface.direction.DOWN) {
            head.y++;
        }
        if (dir == Interface.direction.RIGHT) {
            head.x++;
        }

        // Check if head is on the board and if not, continue the game
        if (head.y < board.length && head.x < board[0].length && head.x >= 0 && head.y >= 0) {

            // Check if head don't hit a part of body of snake and turn of the game if yes
            if (board[head.y][head.x] > 0) {
                /**
                 * Konami part is an add-on of the initial game for bonus
                 * @author Rémi Heredero
                 */
                if (Settings.konami) {
                    snake.length = board[head.y][head.x] - 1;
                    for (int y = 0; y < board.length; y++) {
                        for (int x = 0; x < board[0].length; x++) {
                            if (board[y][x] >= snake.getLength()){ board[y][x] = 0; }
                        }
                    }
                } else {
                    inProgress = false;
                }
            } else {

                // Check if a fruit is on place to the next position of the head of snake and makes the snake grow if yes
                if (fruit.isFruit(board[head.y][head.x])) {
                    snake.growUp(fruit.eatFruit(board[head.y][head.x]));
                }

                // Place the new snake on the board
                board = snake.placeSnake(board, head);

                // Create a Fruit if not enough fruit(s) exist
                if (fruit.getNumberOfFruit() < NUMBEROFFRUIT) {
                    board = fruit.createFruit(board);
                }
            }
        } else {
            inProgress = false;
        }

        // if game is finish, reset the number of fruit and the direction
        if(!inProgress){
            fruit.reset();
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
    public XY positionInBoard(int val) {
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[0].length; x++) {
                if (board[y][x] == val) {
                    return new XY(x, y);
                }
            }
        }
        return null;
    }

    public int getLength() {
        return snake.getLength();
    }
}
