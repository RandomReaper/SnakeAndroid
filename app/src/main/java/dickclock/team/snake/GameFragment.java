package dickclock.team.snake;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.fragment.app.Fragment;

public class GameFragment extends Fragment{

    public static GameView drawingView;
    public static Game snakeGame;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_game, container, false);

        drawingView = new GameView(view.getContext());
        FrameLayout myLayout1 = MainActivity.instance.findViewById(R.id.fragment_container);
        myLayout1.addView(drawingView);

        //this.getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        return view;
    }

    public static void startGame(int x, int y){
        //Log.i(MainActivity.TAG, x + "x" + y);
        snakeGame = new Game(y,x);
        snakeGame.Initialization();
        //Interface.drawOnTerminal(snakeGame.getBoard());
    }

    public static void playOneRound(Interface.direction dir){
        GameView.drawing = true;

        // Drawing the game
        //Interface.drawOnTerminal(snakeGame.getBoard());
        try {drawingView.invalidate();} catch (Exception e) {e.printStackTrace();}

        // Play one round
        //Log.i(MainActivity.TAG, "Direction: " + dir);
        snakeGame.play(dir);
    }


}
