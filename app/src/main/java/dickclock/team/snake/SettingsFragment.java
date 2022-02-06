package dickclock.team.snake;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment implements OnClickListener {

    private View mView;


    interface Listener {
        void onSettingsScreenDismissed();
    }

    private Listener mListener = null;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_settings, container, false);

        final int[] clickableIds = {
                R.id.settings_ok_button
        };

        for (int clickableId : clickableIds) {
            mView.findViewById(clickableId).setOnClickListener(this);
        }

        updateUI();

        return mView;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private void updateUI() {
        if (mView == null) {
            // view has not been created yet, do not do anything
            return;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_ok_button:
                mListener.onSettingsScreenDismissed();
                break;
        }
    }
}
