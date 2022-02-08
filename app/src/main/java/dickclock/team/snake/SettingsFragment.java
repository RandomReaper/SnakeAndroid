package dickclock.team.snake;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment implements OnClickListener {

    private View mView;
    private ToggleButton mHasBorder;
    private ToggleButton mHasGrid;
    private SeekBar mSensitivity;
    private SeekBar mBorderSeekBar;
    private TextView mBorderTextView;
    private SeekBar mGridSeekBar;
    private TextView mGridTextView;
    private SeekBar mHeadSeekBar;
    private SeekBar mBodySeekBar;
    private SeekBar mBackgroundSeekBar;
    private EditText mChanceForBanana;
    private EditText mNbrFruits;
    public static boolean onSettingsView = false;


    interface Listener {
        void onSettingsScreenDismissed();
    }

    private Listener mListener = null;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_settings, container, false);
        mHasBorder = mView.findViewById(R.id.hasBorder_toggle);
        mHasGrid = mView.findViewById(R.id.hasGrid_toggle);
        mSensitivity = mView.findViewById(R.id.sensitivitySeekBar);
        mBorderSeekBar = mView.findViewById(R.id.border_color_SeekBar);
        mBorderTextView = mView.findViewById(R.id.border_textView);
        mGridSeekBar = mView.findViewById(R.id.grid_color_seekBar);
        mGridTextView = mView.findViewById(R.id.grid_textView);
        mHeadSeekBar = mView.findViewById(R.id.head_color_seekBar);
        mBodySeekBar = mView.findViewById(R.id.body_color_seekBar);
        mBackgroundSeekBar = mView.findViewById(R.id.background_color_SeekBar);
        mChanceForBanana = mView.findViewById(R.id.fruits_chance_editTextNumber);
        mNbrFruits = mView.findViewById(R.id.nbrFruits_editTextNumber);

        final int[] clickableIds = {
                R.id.settings_ok_button,
                mHasBorder.getId(),
                mHasGrid.getId(),
                R.id.reset_fun_mode
        };
        
        final int[] seekIds = {
                mBorderSeekBar.getId(),
                mGridSeekBar.getId(),
                mHeadSeekBar.getId(),
                mBodySeekBar.getId(),
                mBackgroundSeekBar.getId()
        };

        for (int clickableId : clickableIds) {
            mView.findViewById(clickableId).setOnClickListener(this);
        }
        for (int seekId : seekIds) {
            SeekBar seekBar = mView.findViewById(seekId);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        seekBar.setBackgroundColor(Color.HSVToColor(getHSVT(progress)));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

        updateUI();

        onSettingsView = true;

        return mView;
    }

    private float[] getHSVT(int hue){
        float[] color = new float[3];
        color[0] = hue;
        color[1] = 1;
        color[2] = 1;
        if(hue==0){color[2] = 0;}
        if(hue==360){color[1] = 0;}
        return color;
    }
    private int getHue(int color){
        float[] hsvt = new float[3];
        Color.colorToHSV(color, hsvt);
        if(color == Color.WHITE){hsvt[0] = 360;}
        return (int)hsvt[0];
    }
    private int getColor(int hue){
        float[] hsvt = new float[3];
        hsvt[0] = hue;
        hsvt[1] = 1;
        hsvt[2] = 1;
        if(hue==0){hsvt[2] = 0;}
        if(hue==360){hsvt[1] = 0;}
        return Color.HSVToColor(hsvt);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private void updateUI() {
        if (mView == null) {
            // view has not been created yet, do not do anything
            return;
        }
        mHasBorder.setChecked(Settings.hasBorder);
        mHasGrid.setChecked(Settings.hasGrid);
        mSensitivity.setProgress(mSensitivity.getMax() - (Settings.sensitivity-5));
        mBorderTextView.setVisibility( mHasBorder.isChecked() ? View.VISIBLE : View.GONE );
        mBorderSeekBar.setVisibility( mHasBorder.isChecked() ? View.VISIBLE : View.GONE );
        mGridTextView.setVisibility( mHasGrid.isChecked() ? View.VISIBLE : View.GONE );
        mGridSeekBar.setVisibility( mHasGrid.isChecked() ? View.VISIBLE : View.GONE );
        mBorderSeekBar.setBackgroundColor(Settings.border);
        mBorderSeekBar.setProgress(getHue(Settings.border));
        mGridSeekBar.setBackgroundColor(Settings.grid);
        mGridSeekBar.setProgress(getHue(Settings.grid));
        mHeadSeekBar.setBackgroundColor(Settings.head);
        mHeadSeekBar.setProgress(getHue(Settings.head));
        mBodySeekBar.setBackgroundColor(Settings.body);
        mBodySeekBar.setProgress(getHue(Settings.body));
        mBackgroundSeekBar.setBackgroundColor(Settings.background);
        mBackgroundSeekBar.setProgress(getHue(Settings.background));
        if (Settings.fruitsChance!=Settings.fruitsChanceBase) {
            mChanceForBanana.setText(String.valueOf(Settings.fruitsChance-Settings.fruitsChanceBase));
        }
        if (Settings.numberOfFruits!=Settings.numberOfFruitsBase) {
            mNbrFruits.setText(String.valueOf(Settings.numberOfFruits));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_ok_button:
                if(Settings.konami){
                    mHeadSeekBar.setBackgroundColor(Settings.head);
                    mHeadSeekBar.setProgress(getHue(Settings.head));
                }
                Settings.sensitivity = (mSensitivity.getMax()-mSensitivity.getProgress())+5;
                Settings.border = getColor(mBorderSeekBar.getProgress());
                Settings.grid = getColor(mGridSeekBar.getProgress());
                Settings.head = getColor(mHeadSeekBar.getProgress());
                Settings.body = getColor(mBodySeekBar.getProgress());
                Settings.background = getColor(mBackgroundSeekBar.getProgress());
                if (!mChanceForBanana.getText().toString().equals("")){
                    Settings.fruitsChance = Integer.parseInt(mChanceForBanana.getText().toString())+Settings.fruitsChanceBase;
                    Settings.funModeOn = true;
                }
                if (!mNbrFruits.getText().toString().equals("")){
                    Settings.numberOfFruits = Integer.parseInt(mNbrFruits.getText().toString());
                    Settings.funModeOn = true;
                }
                Settings.pushSettingsOnMemorize();
                mListener.onSettingsScreenDismissed();
                Log.i(MainActivity.TAG, "Fun mode: " + Settings.funModeOn);
                break;
            case R.id.hasBorder_toggle:
                Settings.hasBorder = mHasBorder.isChecked();
                break;
            case R.id.hasGrid_toggle:
                Settings.hasGrid = mHasGrid.isChecked();
                break;
            case R.id.reset_fun_mode:
                mChanceForBanana.setText("");
                Settings.fruitsChance = Settings.fruitsChanceBase;
                mNbrFruits.setText("");
                Settings.numberOfFruits = Settings.numberOfFruitsBase;
                Settings.funModeOn = false;
        }
        updateUI();
    }
}
