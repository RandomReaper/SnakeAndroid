package dickclock.team.snake;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.util.Locale;

/**
 * Fragment for the gameplay portion of the game. It shows the keypad
 * where the user can request their score.
 *
 * @author Bruno Oliveira (Google)
 */
public class GameplayFragment extends Fragment implements OnClickListener {
    private int mRequestedScore = 5000;

    private TextView mScoreTextView;

    interface Callback {
        // called when the user presses the okay button to submit a score
        void onEnteredScore(int score);
    }

    private Callback mCallback = null;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_gameplay, container, false);

        final int[] clickableIds = {
                R.id.digit_button_0,
                R.id.digit_button_1,
                R.id.digit_button_2,
                R.id.digit_button_3,
                R.id.digit_button_4,
                R.id.digit_button_5,
                R.id.digit_button_6,
                R.id.digit_button_7,
                R.id.digit_button_8,
                R.id.digit_button_9,
                R.id.digit_button_clear,
                R.id.ok_score_button
        };

        for (int clickableId : clickableIds) {
            view.findViewById(clickableId).setOnClickListener(this);
        }

        // cache views
        mScoreTextView = view.findViewById(R.id.text_gameplay_score);

        updateUI();

        return view;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    private void setScore(int score) {
        mRequestedScore = score;
        updateUI();
    }

    private void updateUI() {
        mScoreTextView.setText(String.format(Locale.getDefault(), "%04d", mRequestedScore));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.digit_button_clear:
                setScore(0);
                break;
            case R.id.digit_button_0:
            case R.id.digit_button_1:
            case R.id.digit_button_2:
            case R.id.digit_button_3:
            case R.id.digit_button_4:
            case R.id.digit_button_5:
            case R.id.digit_button_6:
            case R.id.digit_button_7:
            case R.id.digit_button_8:
            case R.id.digit_button_9:
                int x = Integer.parseInt(((Button) view).getText().toString().trim());
                setScore((mRequestedScore * 10 + x) % 10000);
                break;
            case R.id.ok_score_button:
                mCallback.onEnteredScore(mRequestedScore);
                break;
        }
    }
}
