package dickclock.team.snake;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.EventsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.event.Event;
import com.google.android.gms.games.event.EventBuffer;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends FragmentActivity implements
        MainMenuFragment.Listener,
        EndFragment.Listener,
        SettingsFragment.Listener,
        FriendsFragment.Listener,
        SensorEventListener {

    // Fragments
    private MainMenuFragment mMainMenuFragment;
    private GameFragment mGameFragment;
    private EndFragment mEndFragment;
    public SettingsFragment mSettingsFragment;
    public FriendsFragment mFriendsFragment;

    // Client used to sign in with Google APIs
    private GoogleSignInClient mGoogleSignInClient;

    // Client variables
    private AchievementsClient mAchievementsClient;
    private LeaderboardsClient mLeaderboardsClient;
    public EventsClient mEventsClient;
    private PlayersClient mPlayersClient;

    // request codes we use when invoking an external activity
    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;

    // tag for debug logging
    public static final String TAG = "DebugHER";

    // Level for the game
    private Settings.level mLevel = Settings.level.EASY;

    // The display name of the signed in user.
    private String mDisplayName = "";

    // achievements and scores we're pending to push to the cloud
    // (waiting for the user to sign in, for instance)
    private final AccomplishmentsOutbox mOutbox = new AccomplishmentsOutbox();

    public static MainActivity instance;
    private Interface iface = new Interface();

    // Sensors
    private static SensorManager sensorManager;
    private static Sensor gravity;
    private static Sensor accelerometer;
    private static Sensor magnetometer;
    private static int x, y, z;
    private static float[] mGravity;
    private static float[] mAccelerometer;
    private static float[] mGeomagnetic;

    // Time
    private Date currentTime;
    private long startTime;
    private long previousTime = 0;

    // Memory for settings
    public static SharedPreferences settings;
    public static SharedPreferences.Editor editorSettings;

    //FireBase
    public FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mAuth;

    private boolean end = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        instance = this;
        super.onCreate(savedInstanceState);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mAuth = FirebaseAuth.getInstance();


        settings = getSharedPreferences("settings", 0);
        editorSettings = settings.edit();
        Settings.getSettingsFromMemorize();

        setContentView(R.layout.activity_main);

        // Create the client used to sign in to Google services.
        mGoogleSignInClient = GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());

        // Create the fragments used by the UI.
        mMainMenuFragment = new MainMenuFragment();
        mGameFragment = new GameFragment();
        mEndFragment = new EndFragment();
        mSettingsFragment = new SettingsFragment();
        mFriendsFragment = new FriendsFragment();

        // Set the listeners and callbacks of fragment events.
        mMainMenuFragment.setListener(this);
        mEndFragment.setListener(this);
        mSettingsFragment.setListener(this);
        mFriendsFragment.setListener(this);

        // Add initial Main Menu fragment.
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                mMainMenuFragment).commit();

        // Define sensors
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        Settings.gravitySensor = gravity != null;
        if (!Settings.gravitySensor){
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        Log.i(MainActivity.TAG, Settings.gravitySensor ? "GravitySensorOn" : "WithoutGravitySensor");
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

    }

    private void updateUI(FirebaseUser user){

    }

    private void loadAndPrintEvents() {

        mEventsClient.load(true).addOnSuccessListener(eventBufferAnnotatedData -> {
            EventBuffer eventBuffer = eventBufferAnnotatedData.get();

            int count = 0;
            if (eventBuffer != null) {
                count = eventBuffer.getCount();
            }

            Log.i(TAG, "number of events: " + count);

            for (int i = 0; i < count; i++) {
                Event event = eventBuffer.get(i);
                Log.i(TAG, "event: "
                        + event.getName()
                        + " -> "
                        + event.getValue());
            }
        }).addOnFailureListener(e -> handleException(e, getString(R.string.achievements_exception)));
    }

    // Switch UI to the given fragment
    private void switchToFragment(Fragment newFrag) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, newFrag)
                .commit();
    }

    private boolean isNotSignedIn() { return GoogleSignIn.getLastSignedInAccount(this) == null; }

    private void signInSilently() {
        Log.d(TAG, "signInSilently()");
        try {
            mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
                    task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInSilently(): success");
                            onConnected(task.getResult());
                        } else {
                            Log.d(TAG, "signInSilently(): failure"/*, task.getException()*/);
                            onDisconnected();
                        }
                    });
        } catch (Exception ignored) {}
    }

    private void startSignInIntent() { try { startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN); } catch (Exception ignored) {} }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.
        try { signInSilently(); } catch (Exception ignored) {}


        if (Settings.gravitySensor) {
            sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_UI);
        } else {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    private void signOut() {
        Log.d(TAG, "signOut()");

        if (isNotSignedIn()) {
            Log.w(TAG, "signOut() called, but was not signed in!");
            return;
        }
        try {
            mGoogleSignInClient.signOut().addOnCompleteListener(this,
                    task -> {
                        boolean successful = task.isSuccessful();
                        Log.d(TAG, "signOut(): " + (successful ? "success" : "failed"));

                        onDisconnected();
                    });
        } catch (Exception ignored) {}
    }

    @Override
    public void onStartGameRequested(Settings.level level) { startGame(level); }

    @Override
    public void onShowSettingsRequested(){ switchToFragment(mSettingsFragment); }

    @Override
    public void onShowAchievementsRequested() {
        mAchievementsClient.getAchievementsIntent().addOnSuccessListener(
                intent -> startActivityForResult(intent, RC_UNUSED)
        ).addOnFailureListener(
                e -> handleException(e, getString(R.string.achievements_exception))
        );
    }

    @Override
    public void onShowLeaderboardsRequested() {
        mLeaderboardsClient.getAllLeaderboardsIntent().addOnSuccessListener(
                intent -> startActivityForResult(intent, RC_UNUSED)
        ).addOnFailureListener(
                e -> handleException(e, getString(R.string.leaderboards_exception))
        );
    }

    private void handleException(Exception e, String details) {
        int status = 0;

        if (e instanceof ApiException) {
            ApiException apiException = (ApiException) e;
            status = apiException.getStatusCode();
        }

        String message = getString(R.string.status_exception_error, details, status, e);

        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }

    private void startGame(Settings.level level) {
        mLevel = level;
        try {
            startTime = currentTime.getTime();
        } catch (Exception e) {e.printStackTrace();}
        switchToFragment(mGameFragment);
        GameView.level = level;
    }

    public void onEnteredScore(int score) {

        mEndFragment.setScore(score);
        String s = "";
        switch (mLevel){
            case EASY:
                s = getString(R.string.easy_mode_explanation);
                break;
            case MEDIUM:
                s = getString(R.string.medium_mode_explanation);
                break;
            case HARD:
                s = getString(R.string.hard_mode_explanation);
                break;
        }
        mEndFragment.setExplanation(s);

        if (!Settings.funModeOn) {

            mEventsClient.increment(getString(R.string.event_apples_eat_in_total), score);
            Bundle bundle = new Bundle();
            bundle.putDouble(FirebaseAnalytics.Param.VALUE, score);
            bundle.putString(FirebaseAnalytics.Param.VIRTUAL_CURRENCY_NAME, "apple(s)");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.EARN_VIRTUAL_CURRENCY, bundle);

            // check for achievements
            checkForAchievements(score);

            // update leaderboards
            updateLeaderboards(score);
            updateLeaderboardsPerformance(score,currentTime.getTime()-startTime);

            // push those accomplishments to the cloud, if signed in
            pushAccomplishments();

        }


        // switch to the exciting "you won" screen
        switchToFragment(mEndFragment);

    }

    private boolean upNbrApple(int score, int n) { return score >= n; }

    private boolean p10board(int l, int x, int y){ return l > (x*y)/10; }

    /**
     * Check for achievements and unlock the appropriate ones.
     *
     * @param finalScore     the score the user got.
     */
    private void checkForAchievements(int finalScore) {
        // Check if each condition is met; if so, unlock the corresponding achievement.
        int m10 = 10;
        int m25 = 25;
        int m50 = 50;
        int m100 = 100;
        int m42 = 42;
        int m69 = 69;
        if (upNbrApple(finalScore, m100)){
            switch (mLevel){
                case EASY: mOutbox.m100AppleEasyAchievement = true; break;
                case MEDIUM: mOutbox.m100AppleMediumAchievement = true; break;
                case HARD: mOutbox.m100AppleHardAchievement = true; break;
            }
            achievementToast(getString(R.string.achievement_x_apple_toast_text, m100));
        }
        if (upNbrApple(finalScore, m69)){
            mOutbox.m69AppleAchievement = true;
            achievementToast(getString(R.string.achievement_x_apple_toast_text, m69));
        }
        if (upNbrApple(finalScore, m50)){
            switch (mLevel){
                case EASY: mOutbox.m50AppleEasyAchievement = true; break;
                case MEDIUM: mOutbox.m50AppleMediumAchievement = true; break;
                case HARD: mOutbox.m50AppleHardAchievement = true; break;
            }
            achievementToast(getString(R.string.achievement_x_apple_toast_text, m50));
        }
        if (upNbrApple(finalScore, m42)){
            mOutbox.m42AppleAchievement = true;
            achievementToast(getString(R.string.achievement_x_apple_toast_text, m42));
        }
        if (upNbrApple(finalScore, m25)){
            switch (mLevel){
                case EASY: mOutbox.m25AppleEasyAchievement = true; break;
                case MEDIUM: mOutbox.m25AppleMediumAchievement = true; break;
                case HARD: mOutbox.m25AppleHardAchievement = true; break;
            }
            achievementToast(getString(R.string.achievement_x_apple_toast_text, m25));
        }
        if (upNbrApple(finalScore, m10)) {
            switch (mLevel){
                case EASY: mOutbox.m10AppleEasyAchievement = true; break;
                case MEDIUM: mOutbox.m10AppleMediumAchievement = true; break;
                case HARD: mOutbox.m10AppleHardAchievement = true; break;
            }
            achievementToast(getString(R.string.achievement_x_apple_toast_text, m10));
        }

        if (finalScore == 0){
            switch (mLevel){
                case EASY: mOutbox.m100x0ApplesEasyAchievement += 1; break;
                case MEDIUM: mOutbox.m100x0ApplesMediumAchievement += 1; break;
                case HARD: mOutbox.m100x0ApplesHardAchievement += 1; break;
            }
        }

        if (    p10board(GameFragment.snakeGame.getLength(),
                GameFragment.snakeGame.getBoard().length,
                GameFragment.snakeGame.getBoard()[0].length))   {
            switch (mLevel){
                case EASY: mOutbox.p10BoardEasy = true; break;
                case MEDIUM: mOutbox.p10BoardMedium = true; break;
                case HARD: mOutbox.p10BoardHard = true; break;
            }
            achievementToast(getString(R.string.achievement_p10_board_toast_text));
        }
    }

    private void achievementToast(String achievement) {
        // Only show toast if not signed in. If signed in, the standard Google Play
        // toasts will appear, so we don't need to show our own.
        if (isNotSignedIn()) {
            Toast.makeText(this, getString(R.string.achievement) + ": " + achievement,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void pushAccomplishments() {
        if (isNotSignedIn()) {
            // can't push to the cloud, try again later
            return;
        }
        if(Settings.konami){
            mEventsClient.increment(getString(R.string.event_konami_mode_games_played), 1);
        }
        if (mOutbox.m10AppleEasyAchievement) {
            mAchievementsClient.unlockImmediate(getString(R.string.achievement_10_apples_easy_mode));
            mOutbox.m10AppleEasyAchievement = false;
        }
        if (mOutbox.m10AppleMediumAchievement) {
            mAchievementsClient.unlockImmediate(getString(R.string.achievement_10_apples_medium_mode));
            mOutbox.m10AppleMediumAchievement = false;
        }
        if (mOutbox.m10AppleHardAchievement) {
            mAchievementsClient.unlockImmediate(getString(R.string.achievement_10_apples_hard_mode));
            mOutbox.m10AppleHardAchievement = false;
        }
        if (mOutbox.m25AppleEasyAchievement) {
            mAchievementsClient.unlockImmediate(getString(R.string.achievement_25_apples_easy_mode));
            mOutbox.m25AppleEasyAchievement = false;
        }
        if (mOutbox.m25AppleMediumAchievement) {
            mAchievementsClient.unlockImmediate(getString(R.string.achievement_25_apples_medium_mode));
            mOutbox.m25AppleMediumAchievement = false;
        }
        if (mOutbox.m25AppleHardAchievement) {
            mAchievementsClient.unlockImmediate(getString(R.string.achievement_25_apples_hard_mode));
            mOutbox.m25AppleHardAchievement = false;
        }
        if (mOutbox.m50AppleEasyAchievement) {
            mAchievementsClient.unlockImmediate(getString(R.string.achievement_50_apples_easy_mode));
            mOutbox.m50AppleEasyAchievement = false;
        }
        if (mOutbox.m50AppleMediumAchievement) {
            mAchievementsClient.unlockImmediate(getString(R.string.achievement_50_apples_medium_mode));
            mOutbox.m50AppleMediumAchievement = false;
        }
        if (mOutbox.m50AppleHardAchievement) {
            mAchievementsClient.unlockImmediate(getString(R.string.achievement_50_apples_hard_mode));
            mOutbox.m50AppleHardAchievement = false;
        }
        if (mOutbox.m100AppleEasyAchievement) {
            mAchievementsClient.unlockImmediate(getString(R.string.achievement_100_apples_easy_mode));
            mOutbox.m100AppleEasyAchievement = false;
        }
        if (mOutbox.m100AppleMediumAchievement) {
            mAchievementsClient.unlockImmediate(getString(R.string.achievement_100_apples_medium_mode));
            mOutbox.m100AppleMediumAchievement = false;
        }
        if (mOutbox.m100AppleHardAchievement) {
            mAchievementsClient.unlockImmediate(getString(R.string.achievement_100_apples_hard_mode));
            mOutbox.m100AppleHardAchievement = false;
        }
        if (mOutbox.m42AppleAchievement) {
            mAchievementsClient.unlockImmediate(getString(R.string.achievement_42_apples));
            mOutbox.m42AppleAchievement = false;
        }
        if (mOutbox.m69AppleAchievement) {
            mAchievementsClient.unlockImmediate(getString(R.string.achievement_69_apples));
            mOutbox.m69AppleAchievement = false;
        }
        if (mOutbox.m100x0ApplesEasyAchievement >= 1) {
            mAchievementsClient.incrementImmediate(getString(R.string.achievement_100x_0_apples_easy_mode),1);
            mOutbox.m100x0ApplesEasyAchievement = 0;
        }
        if (mOutbox.m100x0ApplesMediumAchievement >= 1) {
            mAchievementsClient.incrementImmediate(getString(R.string.achievement_100x_0_apples_medium_mode),1);
            mOutbox.m100x0ApplesMediumAchievement = 0;
        }
        if (mOutbox.m100x0ApplesHardAchievement >= 1) {
            mAchievementsClient.incrementImmediate(getString(R.string.achievement_100x_0_apples_hard_mode),1);
            mOutbox.m100x0ApplesHardAchievement = 0;
        }
        if (mOutbox.p10BoardEasy) {
            mAchievementsClient.unlockImmediate(getString(R.string.achievement_10_of_the_game_board_filled_in_easy_mode));
            mOutbox.p10BoardEasy = false;
        }
        if (mOutbox.p10BoardMedium) {
            mAchievementsClient.unlockImmediate(getString(R.string.achievement_10_of_the_game_board_filled_in_medium_mode));
            mOutbox.p10BoardMedium = false;
        }
        if (mOutbox.p10BoardHard) {
            mAchievementsClient.unlockImmediate(getString(R.string.achievement_10_of_the_game_board_filled_in_hard_mode));
            mOutbox.p10BoardHard = false;
        }
        if (mOutbox.mEasyModeScore >= 0) {
            mLeaderboardsClient.submitScoreImmediate(getString(R.string.leaderboard_easy_high_scores),
                    mOutbox.mEasyModeScore);
            mOutbox.mEasyModeScore = -1;
        }
        if (mOutbox.mMediumModeScore >= 0) {
            mLeaderboardsClient.submitScoreImmediate(getString(R.string.leaderboard_medium_high_scores),
                    mOutbox.mMediumModeScore);
            mOutbox.mMediumModeScore = -1;
        }
        if (mOutbox.mHardModeScore >= 0) {
            mLeaderboardsClient.submitScoreImmediate(getString(R.string.leaderboard_hard_high_scores),
                    mOutbox.mHardModeScore);
            mOutbox.mHardModeScore = -1;
        }
        if (mOutbox.mEasyModePerformance >= 0) {
            mLeaderboardsClient.submitScoreImmediate(getString(R.string.leaderboard_easy_performance),
                    mOutbox.mEasyModePerformance);
            mOutbox.mEasyModePerformance = -1;
        }
        if (mOutbox.mMediumModePerformance >= 0) {
            mLeaderboardsClient.submitScoreImmediate(getString(R.string.leaderboard_medium_performance),
                    mOutbox.mMediumModePerformance);
            mOutbox.mMediumModePerformance = -1;
        }
        if (mOutbox.mHardModePerformance >= 0) {
            mLeaderboardsClient.submitScoreImmediate(getString(R.string.leaderboard_hard_performance),
                    mOutbox.mHardModePerformance);
            mOutbox.mHardModePerformance = -1;
        }
    }

    public PlayersClient getPlayersClient() { return mPlayersClient; }

    public String getDisplayName() { return mDisplayName; }

    /**
     * Update leaderboards with the user's score.
     *
     * @param finalScore The score the user got.
     */
    private void updateLeaderboards(int finalScore) {
        if (mLevel == Settings.level.HARD && mOutbox.mHardModeScore < finalScore) {
            mOutbox.mHardModeScore = finalScore;
        } else if (mLevel == Settings.level.MEDIUM && mOutbox.mMediumModeScore < finalScore) {
            mOutbox.mMediumModeScore = finalScore;
        } else if (mLevel == Settings.level.EASY && mOutbox.mEasyModeScore < finalScore) {
            mOutbox.mEasyModeScore = finalScore;
        }
    }
    private void updateLeaderboardsPerformance(int finalScore, long time) {
        if (finalScore < 1){
            return;
        }
        long score = (long) (time/(finalScore*1.1));
        //Log.i(TAG, "Performance: " + score);
        if (mLevel == Settings.level.HARD && mOutbox.mHardModePerformance < score) {
            mOutbox.mHardModePerformance = score;
        } else if (mLevel == Settings.level.MEDIUM && mOutbox.mMediumModePerformance < score) {
            mOutbox.mMediumModePerformance = score;
        } else if (mLevel == Settings.level.EASY && mOutbox.mEasyModePerformance < score) {
            mOutbox.mEasyModePerformance = score;
        }
    }

    @Override
    public void onEndScreenDismissed() { switchToFragment(mMainMenuFragment); }

    @Override
    public void onSettingsScreenDismissed() {
        SettingsFragment.onSettingsView = false;
        switchToFragment(mMainMenuFragment);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(intent);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                onConnected(account);
            } catch (ApiException apiException) {
                String message = apiException.getMessage();
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.signin_other_error);
                }

                onDisconnected();

                try {
                    new AlertDialog.Builder(this)
                            .setMessage(message)
                            .setNeutralButton(android.R.string.ok, null)
                            .show();
                } catch (Exception ignored) {}
            }
        }
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "onConnected(): connected to Google APIs");

        mAchievementsClient = Games.getAchievementsClient(this, googleSignInAccount);
        mLeaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount);
        mEventsClient = Games.getEventsClient(this, googleSignInAccount);
        mPlayersClient = Games.getPlayersClient(this, googleSignInAccount);

        // Show sign-out button on main menu
        mMainMenuFragment.setShowSignInButton(false);

        // Show "you are signed in" message on win screen, with no sign in button.
        mEndFragment.setShowSignInButton(false);

        // Set the greeting appropriately on main menu
        mPlayersClient.getCurrentPlayer()
                .addOnCompleteListener(task -> {
                    String displayName;
                    if (task.isSuccessful()) {
                        displayName = Objects.requireNonNull(task.getResult()).getDisplayName();
                    } else {
                        Exception e = task.getException();
                        handleException(e, getString(R.string.players_exception));
                        displayName = "???";
                    }
                    mDisplayName = displayName;
                    mMainMenuFragment.setGreeting("Hello, " + displayName);
                });


        // if we have accomplishments to push, push them
        if (!mOutbox.isEmpty()) {
            pushAccomplishments();
            Toast.makeText(this, getString(R.string.your_progress_will_be_uploaded),
                    Toast.LENGTH_LONG).show();
        }

        loadAndPrintEvents();
    }

    private void onDisconnected() {
        Log.d(TAG, "onDisconnected()");

        mAchievementsClient = null;
        mLeaderboardsClient = null;
        mPlayersClient = null;

        // Show sign-in button on main menu
        mMainMenuFragment.setShowSignInButton(true);

        // Show sign-in button on win screen
        mEndFragment.setShowSignInButton(true);

        mMainMenuFragment.setGreeting(getString(R.string.signed_out_greeting));
    }

    @Override
    public void onSignInButtonClicked() { try { startSignInIntent(); } catch (Exception ignored) {} }

    @Override
    public void onSignOutButtonClicked() { try { signOut(); } catch (Exception ignored) {} }

    @Override
    public void onShowFriendsButtonClicked() { switchToFragment(mFriendsFragment); }

    @Override
    public void onBackButtonClicked() { switchToFragment(mMainMenuFragment); }

    @Override
    public void onSensorChanged(SensorEvent event) {
        currentTime = Calendar.getInstance().getTime();
        boolean play = false;

        if (Settings.gravitySensor){
            if (event.sensor.getType() == Sensor.TYPE_GRAVITY) { mGravity = event.values; }
            if (mGravity != null){
                play = true;
                double foo = 90/9.8;
                x = (int) (foo * event.values[0]);
                y = (int) (foo * event.values[1]);
            }
        } else {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) { mAccelerometer = event.values; }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) { mGeomagnetic = event.values; }
            if (mAccelerometer != null && mGeomagnetic != null){
                play = true;
                float[] R = new float[9];
                float[] I = new float[9];
                if ( SensorManager.getRotationMatrix(R, I, mAccelerometer, mGeomagnetic) ) {
                    float[] orientation = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    x = (int) Math.toDegrees( orientation[2] );
                    y = (int) Math.toDegrees( orientation[1] );
                }
            }
        }

        if(play){
            Settings.addKonami(iface.getNextDir(x,y));
            try {
                if (GameFragment.snakeGame.isInProgress()){
                    end = false;

                    long time = currentTime.getTime();
                    if((time-previousTime) >= Settings.getTime(mLevel)){
                        previousTime = time;
                        GameFragment.playOneRound(iface.getNextDir(x,y));
                    }

                } else if (!end){
                    end = true;
                    GameView.drawing = false;
                    GameFragment.drawingView.invalidate();
                    int score = GameFragment.snakeGame.getScore();
                    switchToFragment(mEndFragment);
                    iface.putScoreTerminal(score);
                    iface.nextDir = Interface.direction.NOCHANGE;
                    onEnteredScore(score);
                }
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private static class AccomplishmentsOutbox {
        boolean m10AppleEasyAchievement = false;
        boolean m10AppleMediumAchievement = false;
        boolean m10AppleHardAchievement = false;
        boolean m25AppleEasyAchievement = false;
        boolean m25AppleMediumAchievement = false;
        boolean m25AppleHardAchievement = false;
        boolean m50AppleEasyAchievement = false;
        boolean m50AppleMediumAchievement = false;
        boolean m50AppleHardAchievement = false;
        boolean m100AppleEasyAchievement = false;
        boolean m100AppleMediumAchievement = false;
        boolean m100AppleHardAchievement = false;
        boolean m42AppleAchievement = false;
        boolean m69AppleAchievement = false;
        int m100x0ApplesEasyAchievement = 0;
        int m100x0ApplesMediumAchievement = 0;
        int m100x0ApplesHardAchievement = 0;
        boolean p10BoardEasy = false;
        boolean p10BoardMedium = false;
        boolean p10BoardHard = false;
        int mEasyModeScore = -1;
        int mMediumModeScore = -1;
        int mHardModeScore = -1;
        long mEasyModePerformance = -1;
        long mMediumModePerformance = -1;
        long mHardModePerformance = -1;

        boolean isEmpty() {
            boolean out;
            out = !m10AppleEasyAchievement;
            out = out && !m10AppleMediumAchievement;
            out = out && !m10AppleHardAchievement;
            out = out && !m25AppleEasyAchievement;
            out = out && !m25AppleMediumAchievement;
            out = out && !m25AppleHardAchievement;
            out = out && !m50AppleEasyAchievement;
            out = out && !m50AppleMediumAchievement;
            out = out && !m50AppleHardAchievement;
            out = out && !m100AppleEasyAchievement;
            out = out && !m100AppleMediumAchievement;
            out = out && !m100AppleHardAchievement;
            out = out && !m42AppleAchievement;
            out = out && !m69AppleAchievement;
            out = out && m100x0ApplesEasyAchievement == 0;
            out = out && m100x0ApplesMediumAchievement == 0;
            out = out && m100x0ApplesHardAchievement == 0;
            out = out && !p10BoardEasy;
            out = out && !p10BoardMedium;
            out = out && !p10BoardHard;
            out = out && mEasyModeScore < 0;
            out = out && mMediumModeScore < 0;
            out = out && mHardModeScore < 0;
            out = out && mEasyModePerformance < 0;
            out = out && mMediumModePerformance < 0;
            out = out && mHardModePerformance < 0;

            return out;
        }

    }
}
