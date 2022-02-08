package dickclock.team.snake;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

/**
 * Fragment that shows the end message with score
 * @author Rémi Heredero
 */
public class EndFragment extends Fragment implements OnClickListener {
    private String mExplanation = "";
    private int mScore = 0;
    private boolean mShowSignIn = false;

    // cached views
    private TextView mScoreTextView;
    private TextView mExplanationTextView;
    private View mSignInBar;
    private View mSignedInBar;
    private View mView;

    interface Listener {
        // called when the user presses the `Ok` button
        void onEndScreenDismissed();

        // called when the user presses the `Sign In` button
        void onSignInButtonClicked();
    }

    private Listener mListener = null;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_end, container, false);

        final int[] clickableIds = {
                R.id.win_ok_button,
                R.id.win_screen_sign_in_button
        };

        for (int clickableId : clickableIds) {
            mView.findViewById(clickableId).setOnClickListener(this);
        }

        // cache views
        mScoreTextView = mView.findViewById(R.id.text_win_score);
        mExplanationTextView = mView.findViewById(R.id.text_explanation);
        mSignInBar = mView.findViewById(R.id.win_sign_in_bar);
        mSignedInBar = mView.findViewById(R.id.signed_in_bar);

        updateUI();

        return mView;
    }

    public void setScore(int score) {
        mScore = score;
        updateUI();
    }

    public void setExplanation(String explanation) {
        mExplanation = explanation;
        updateUI();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private void updateUI() {
        if (mView == null) {
            // view has not been created yet, do not do anything
            return;
        }

        mScoreTextView.setText(String.valueOf(mScore));
        mExplanationTextView.setText(mExplanation);

        //TODO for release:
        mSignInBar.setVisibility(mShowSignIn ? View.VISIBLE : View.GONE);
        //mSignInBar.setVisibility(View.GONE);
        //mSignedInBar.setVisibility(View.GONE);
        mSignedInBar.setVisibility(mShowSignIn ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.win_screen_sign_in_button:
                mListener.onSignInButtonClicked();
                break;
            case R.id.win_ok_button:
                mListener.onEndScreenDismissed();
                break;
        }
    }

    public void setShowSignInButton(boolean showSignIn) {
        mShowSignIn = showSignIn;
        updateUI();
    }
}
