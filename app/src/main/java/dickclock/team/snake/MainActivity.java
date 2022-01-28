package dickclock.team.snake;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.EventsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.event.Event;
import com.google.android.gms.games.event.EventBuffer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends FragmentActivity implements
        MainMenuFragment.Listener,
        GameplayFragment.Callback,
        WinFragment.Listener,
        FriendsFragment.Listener {

    // Fragments
    private MainMenuFragment mMainMenuFragment;
    private GameplayFragment mGameplayFragment;
    private WinFragment mWinFragment;
    public FriendsFragment mFriendsFragment;

    // Client used to sign in with Google APIs
    private GoogleSignInClient mGoogleSignInClient;

    // Client variables
    private AchievementsClient mAchievementsClient;
    private LeaderboardsClient mLeaderboardsClient;
    private EventsClient mEventsClient;
    private PlayersClient mPlayersClient;

    // request codes we use when invoking an external activity
    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;

    // tag for debug logging
    private static final String TAG = "DebugHER";

    // playing on hard mode?
    public enum level {EASY, MEDIUM, HARD};
    private level mLevel = level.EASY;

    private boolean mHardMode = false;

    // The diplay name of the signed in user.
    private String mDisplayName = "";

    // achievements and scores we're pending to push to the cloud
    // (waiting for the user to sign in, for instance)
    private final AccomplishmentsOutbox mOutbox = new AccomplishmentsOutbox();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Create the client used to sign in to Google services.
        mGoogleSignInClient = GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());

        // Create the fragments used by the UI.
        mMainMenuFragment = new MainMenuFragment();
        mGameplayFragment = new GameplayFragment();
        mWinFragment = new WinFragment();
        mFriendsFragment = new FriendsFragment();

        // Set the listeners and callbacks of fragment events.
        mMainMenuFragment.setListener(this);
        mGameplayFragment.setCallback(this);
        mWinFragment.setListener(this);
        mFriendsFragment.setListener(this);

        // Add initial Main Menu fragment.
        // IMPORTANT: if this Activity supported rotation, we'd have to be
        // more careful about adding the fragment, since the fragment would
        // already be there after rotation and trying to add it again would
        // result in overlapping fragments. But since we don't support rotation,
        // we don't deal with that for code simplicity.
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                mMainMenuFragment).commit();

        checkPlaceholderIds();
    }

    // Check the sample to ensure all placeholder ids are are updated with real-world values.
    // This is strictly for the purpose of the samples; you don't need this in a production
    // application.
    private void checkPlaceholderIds() {
        StringBuilder problems = new StringBuilder();
/*
        if (getPackageName().startsWith("com.google.")) {
            problems.append("- Package name start with com.google.*\n");
        }

        for (Integer id : new Integer[]{
                R.string.app_id,
                R.string.achievement_10_apples_ate,
                R.string.leaderboard_easy_high_scores,
                R.string.leaderboard_medium_high_scores,
                R.string.leaderboard_hard_high_scores,
        }) {

            String value = getString(id);

            if (value.startsWith("YOUR_")) {
                // needs replacing
                problems.append("- Placeholders(YOUR_*) in ids.xml need updating\n");
                break;
            }
        }

 */

        if (problems.length() > 0) {
            problems.insert(0, "The following problems were found:\n\n");

            problems.append("\nThese problems may prevent the app from working properly.");
            problems.append("\n\nSee the TODO window in Android Studio for more information");
            (new AlertDialog.Builder(this)).setMessage(problems.toString())
                    .setNeutralButton(android.R.string.ok, null).create().show();
        }
    }

    private void loadAndPrintEvents() {

        final MainActivity mainActivity = this;

        mEventsClient.load(true)
                .addOnSuccessListener(new OnSuccessListener<AnnotatedData<EventBuffer>>() {
                    @Override
                    public void onSuccess(AnnotatedData<EventBuffer> eventBufferAnnotatedData) {
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
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        handleException(e, getString(R.string.achievements_exception));
                    }
                });
    }

    // Switch UI to the given fragment
    private void switchToFragment(Fragment newFrag) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, newFrag)
                .commit();
    }

    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }

    private void signInSilently() {
        Log.d(TAG, "signInSilently()");

        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInSilently(): success");
                            onConnected(task.getResult());
                        } else {
                            Log.d(TAG, "signInSilently(): failure", task.getException());
                            onDisconnected();
                        }
                    }
                });
    }

    private void startSignInIntent() {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.
        signInSilently();
    }

    private void signOut() {
        Log.d(TAG, "signOut()");

        if (!isSignedIn()) {
            Log.w(TAG, "signOut() called, but was not signed in!");
            return;
        }

        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        boolean successful = task.isSuccessful();
                        Log.d(TAG, "signOut(): " + (successful ? "success" : "failed"));

                        onDisconnected();
                    }
                });
    }

    @Override
    public void onStartGameRequested(level level) {
        startGame(level);
    }

    @Override
    public void onShowAchievementsRequested() {
        mAchievementsClient.getAchievementsIntent()
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_UNUSED);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        handleException(e, getString(R.string.achievements_exception));
                    }
                });
    }

    @Override
    public void onShowLeaderboardsRequested() {
        mLeaderboardsClient.getAllLeaderboardsIntent()
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_UNUSED);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        handleException(e, getString(R.string.leaderboards_exception));
                    }
                });
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


    private void startGame(level level) {
        mLevel = level;
        switchToFragment(mGameplayFragment);
    }

    @Override
    public void onEnteredScore(int requestedScore) {
        int finalScore = 0;
        switch (mLevel){
            case EASY:
                finalScore = requestedScore;
                break;
            case MEDIUM:
                finalScore = requestedScore/2;
                break;
            case HARD:
                finalScore = requestedScore/3;
                break;
        }

        mWinFragment.setScore(finalScore);
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
        mWinFragment.setExplanation(s);

        // check for achievements
        checkForAchievements(requestedScore, finalScore);

        // update leaderboards
        updateLeaderboards(finalScore);

        // push those accomplishments to the cloud, if signed in
        pushAccomplishments();

        // switch to the exciting "you won" screen
        switchToFragment(mWinFragment);

    }

    // Checks if n is prime. We don't consider 0 and 1 to be prime.
    // This is not an implementation we are mathematically proud of, but it gets the job done.
    private boolean up10Apple(int n) {
        return n >= 10;
    }

    /**
     * Check for achievements and unlock the appropriate ones.
     *
     * @param requestedScore the score the user requested.
     * @param finalScore     the score the user got.
     */
    private void checkForAchievements(int requestedScore, int finalScore) {
        // Check if each condition is met; if so, unlock the corresponding
        // achievement.
        if (up10Apple(finalScore)) {
            mOutbox.m10AppleAchievement = true;
            achievementToast(getString(R.string.achievement_10_apples_ate));
        }
    }

    private void achievementToast(String achievement) {
        // Only show toast if not signed in. If signed in, the standard Google Play
        // toasts will appear, so we don't need to show our own.
        if (!isSignedIn()) {
            Toast.makeText(this, getString(R.string.achievement) + ": " + achievement,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void pushAccomplishments() {
        if (!isSignedIn()) {
            // can't push to the cloud, try again later
            return;
        }
        if (mOutbox.m10AppleAchievement) {
            mAchievementsClient.unlock(getString(R.string.achievement_10_apples_ate));
            mOutbox.m10AppleAchievement = false;
        }
        if (mOutbox.mEasyModeScore >= 0) {
            mLeaderboardsClient.submitScore(getString(R.string.leaderboard_easy_high_scores),
                    mOutbox.mEasyModeScore);
            mOutbox.mEasyModeScore = -1;
        }
        if (mOutbox.mMediumModeScore >= 0) {
            mLeaderboardsClient.submitScore(getString(R.string.leaderboard_medium_high_scores),
                    mOutbox.mMediumModeScore);
            mOutbox.mMediumModeScore = -1;
        }
        if (mOutbox.mHardModeScore >= 0) {
            mLeaderboardsClient.submitScore(getString(R.string.leaderboard_hard_high_scores),
                    mOutbox.mHardModeScore);
            mOutbox.mHardModeScore = -1;
        }
    }

    public PlayersClient getPlayersClient() {
        return mPlayersClient;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    /**
     * Update leaderboards with the user's score.
     *
     * @param finalScore The score the user got.
     */
    private void updateLeaderboards(int finalScore) {
        if (mLevel == level.HARD && mOutbox.mHardModeScore < finalScore) {
            mOutbox.mHardModeScore = finalScore;
        } else if (mLevel == level.MEDIUM && mOutbox.mMediumModeScore < finalScore) {
            mOutbox.mMediumModeScore = finalScore;
        } else if (mLevel == level.EASY && mOutbox.mEasyModeScore < finalScore) {
            mOutbox.mEasyModeScore = finalScore;
        }
/*
        if (mHardMode && mOutbox.mHardModeScore < finalScore) {
            mOutbox.mHardModeScore = finalScore;
        } else if (!mHardMode && mOutbox.mEasyModeScore < finalScore) {
            mOutbox.mEasyModeScore = finalScore;
        }
 */
    }

    @Override
    public void onWinScreenDismissed() {
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

                new AlertDialog.Builder(this)
                        .setMessage(message)
                        .setNeutralButton(android.R.string.ok, null)
                        .show();
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
        mWinFragment.setShowSignInButton(false);

        // Set the greeting appropriately on main menu
        mPlayersClient.getCurrentPlayer()
                .addOnCompleteListener(new OnCompleteListener<Player>() {
                    @Override
                    public void onComplete(@NonNull Task<Player> task) {
                        String displayName;
                        if (task.isSuccessful()) {
                            displayName = task.getResult().getDisplayName();
                        } else {
                            Exception e = task.getException();
                            handleException(e, getString(R.string.players_exception));
                            displayName = "???";
                        }
                        mDisplayName = displayName;
                        mMainMenuFragment.setGreeting("Hello, " + displayName);
                    }
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
        mWinFragment.setShowSignInButton(true);

        mMainMenuFragment.setGreeting(getString(R.string.signed_out_greeting));
    }

    @Override
    public void onSignInButtonClicked() {
        startSignInIntent();
    }

    @Override
    public void onSignOutButtonClicked() {
        signOut();
    }

    @Override
    public void onShowFriendsButtonClicked() {
        switchToFragment(mFriendsFragment);
    }

    @Override
    public void onBackButtonClicked() {
        switchToFragment(mMainMenuFragment);
    }

    private class AccomplishmentsOutbox {
        boolean m10AppleAchievement = false;
        int mEasyModeScore = -1;
        int mMediumModeScore = -1;
        int mHardModeScore = -1;

        boolean isEmpty() {
            return !m10AppleAchievement && mEasyModeScore < 0 &&
                    mMediumModeScore < 0 && mHardModeScore < 0;
        }

    }
}
