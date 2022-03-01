package dickclock.team.snake;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

public class GameView extends View {

    private final Paint border = new Paint(Paint.LINEAR_TEXT_FLAG);
    private final Paint grid = new Paint(Paint.LINEAR_TEXT_FLAG);
    private final Paint head = new Paint(Paint.LINEAR_TEXT_FLAG);
    private final Paint body = new Paint(Paint.LINEAR_TEXT_FLAG);
    private final Paint apple = new Paint(Paint.LINEAR_TEXT_FLAG);
    private final Paint banana = new Paint(Paint.LINEAR_TEXT_FLAG);
    private final Paint konami = new Paint(Paint.LINEAR_TEXT_FLAG);
    public static boolean drawing = true;

    private int sizeOfSquare;
    private static int sizeX;
    private static int sizeY;
    private boolean gameStarted = false;
    private boolean initDraw = true;
    private int w;
    private int h;
    private int offsetX;
    private int offsetY;
    public static Settings.level level;

    public GameView(Context context){
        super(context);

        // Define color
        border.setColor(Settings.border);
        grid.setColor(Settings.grid);
        head.setColor(Settings.head);
        body.setColor(Settings.body);
        apple.setColor(Settings.apple);
        banana.setColor(Settings.banana);

        // Define Style
        border.setStyle(Paint.Style.STROKE);
        grid.setStyle(Paint.Style.STROKE);
        head.setStyle(Paint.Style.FILL);
        body.setStyle(Paint.Style.FILL);
        apple.setStyle(Paint.Style.FILL);
        banana.setStyle(Paint.Style.FILL);
        konami.setStyle(Paint.Style.FILL);
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    @Override
    protected void onDraw(Canvas canvas){
        if (initDraw){
            initDraw = false;

            w = getWidth();
            h = getHeight();
            //Log.i(MainActivity.TAG,"Pixels: " + h + "x" + w);

            sizeOfSquare = w / Settings.getSize(level);
            //Log.i(MainActivity.TAG,"Size of square: " + sizeOfSquare);

            sizeX = w /sizeOfSquare;
            sizeY = h /sizeOfSquare;
            //Log.i(MainActivity.TAG,"Size: " + sizeX + "x" + sizeY);

            startGame();

            offsetX = (int) ((w - sizeX * sizeOfSquare) / 2.0);
            offsetY = (int) ((h - sizeY * sizeOfSquare) / 2.0);
            //Log.i(MainActivity.TAG,"Offset: " + offsetX + "x" + offsetY);
        }

        if(drawing && !initDraw){

            canvas.drawColor(Settings.background);

            // Draw border
            if (Settings.hasBorder){
                border.setStrokeWidth(offsetX);
                canvas.drawLine(offsetX/2,0,offsetX/2, h,border);
                canvas.drawLine(w -offsetX/2,0, w -offsetX/2, h,border);
                //noinspection SuspiciousNameCombination
                border.setStrokeWidth(offsetY);
                canvas.drawLine(0,offsetY/2, w,offsetY/2,border);
                canvas.drawLine(0, h -offsetY/2, w, h -offsetY/2,border);
            }

            // Draw grid
            if (Settings.hasGrid){
                for (int x = offsetX; x <= w -offsetX; x+= sizeOfSquare) {
                    canvas.drawLine(x,offsetY,x, h -offsetY,grid);
                }
                for (int y = offsetY; y <= h -offsetY; y+= sizeOfSquare) {
                    canvas.drawLine(offsetX,y, w -offsetX,y,grid);
                }
            }

            // Draw board
            int[][] board = GameFragment.snakeGame.getBoard();

            for (int y = 0; y < board.length; y++) {
                for (int x = 0; x < board[0].length; x++) {
                    int startX = x * sizeOfSquare + offsetX;
                    int startY = y * sizeOfSquare + offsetY;
                    int endX = startX + sizeOfSquare;
                    int endY = startY + sizeOfSquare;

                    //Draw fruits
                    if (board[y][x] == Fruit.VALUEFORAPPLE) { canvas.drawRect(startX,startY,endX,endY,apple); }
                    if (board[y][x] == Fruit.VALUEFORBANANA) { canvas.drawRect(startX,startY,endX,endY,banana); }

                    // Special Konami code
                    if (Settings.konami) {
                        if (board[y][x] >= 1) {
                            int snakeLength = GameFragment.snakeGame.getLength();
                            int boardValue = board[y][x];
                            @SuppressLint("DrawAllocation") int[] rainbowValue = new int[snakeLength];
                            for (int i = 0; i < rainbowValue.length; i++) {
                                int n = i;
                                while (n >= Settings.konamiColor.length) { n -= Settings.konamiColor.length; }
                                rainbowValue[i] = n;
                            }
                            konami.setColor(Settings.konamiColor[ rainbowValue[boardValue-1] ]);
                            canvas.drawRect(startX,startY,endX,endY,konami);
                        }
                    } else {
                        // Draw body of snake
                        if (board[y][x] > 1) { canvas.drawRect(startX,startY,endX,endY,body); }

                        // Draw head of snake
                        if (board[y][x] == 1) { canvas.drawRect(startX,startY,endX,endY,head); }
                    }
                }
            }

        }
    }


    private void startGame(){
        if(!gameStarted){
            GameFragment.startGame(sizeX, sizeY);
            gameStarted = true;
        }
    }
}
