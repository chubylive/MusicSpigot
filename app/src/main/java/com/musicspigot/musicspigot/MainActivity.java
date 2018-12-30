package com.musicspigot.musicspigot;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.ErrorCallback;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.Capabilities;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Repeat;
import com.spotify.protocol.types.Track;

import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private static final String TAG = MainActivity.class.getSimpleName();

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private static final String CLIENT_ID = "28f294f65e454bc39194baf504a1a4da";
    private static final String REDIRECT_URI = "MusicSpigot://callback";
    private SpotifyAppRemote mSpotifyAppRemote;
    private final ErrorCallback mErrorCallback = throwable -> logError(throwable, "Boom!");
    private static HashMap<String, ToSkipToWait> modeMap = new HashMap<String, ToSkipToWait>();
    private String name = "";
    Timer timer = new Timer();

    Subscription<PlayerState> mPlayerStateSubscription;
    Subscription<PlayerContext> mPlayerContextSubscription;
    Subscription<Capabilities> mCapabilitiesSubscription;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    Button prevTrack, playPauseTrack, nextTrack;
    private final Subscription.EventCallback<PlayerState> mPlayerStateEventCallback = new Subscription.EventCallback<PlayerState>() {
        @Override
        public void onEvent(PlayerState playerState) {
            Log.i(TAG + "  " + playerState.track.name, ": my playerstart: " + playerState.track.duration + ": " +  playerState.playbackPosition);

//            if(!name.equals(playerState.track.name)){
//
//                Log.i(TAG + "  " + playerState.track.name, ": my playerstart: " + playerState.track.duration + ": " +  playerState.playbackPosition);
//                Log.i(TAG + "  " + playerState.track.name,instance.toString());
//                if(instance.toWait()){
//                    mSpotifyAppRemote.getPlayerApi().pause();
//                    //schedule wait to play
//                    TimerTask resumePlay = new ResumePlayTask( mSpotifyAppRemote.getPlayerApi());
//                    //multiply by 1000 to get milli-seconds
//                    timer.schedule(resumePlay, instance.getWaitTimeInSeconds() * 1000);
//                    timer.cancel();
//
//                }
//                name = playerState.track.name;
//            }


            }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO check if map has been saved
        // savedInstanceState.get("map");
        //if map does not exit in save states then create new instance
        setContentView(R.layout.activity_main);
        modeMap.put("STEADY", new ToSkipToWait(0, 0, 0, ToSkipToWait.SkipState.STEADY));
        modeMap.put("RANDOM", new ToSkipToWait(0, 0, 0, ToSkipToWait.SkipState.RANDOM));
        modeMap.put("RAMP UP", new ToSkipToWait(0, 0, 0, ToSkipToWait.SkipState.RAMP_UP));
        modeMap.put("RAMP DOWN", new ToSkipToWait(0, 0, 0, ToSkipToWait.SkipState.RAMP_DOWN));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three

        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        prevTrack = findViewById(R.id.prevTrack);
        nextTrack = findViewById(R.id.nextTrack);
        playPauseTrack = findViewById(R.id.playPauseTrack);

    }

    @Override
    public void onStart() {
        super.onStart();
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");
                        //subscribe to events
                        if (mPlayerStateSubscription != null && !mPlayerStateSubscription.isCanceled()) {
                            mPlayerStateSubscription.cancel();
                            mPlayerStateSubscription = null;
                        }


                        mPlayerStateSubscription = (Subscription<PlayerState>) mSpotifyAppRemote.getPlayerApi()
                                .subscribeToPlayerState()
                                .setEventCallback(mPlayerStateEventCallback)
                                .setLifecycleCallback(new Subscription.LifecycleCallback() {
                                    @Override
                                    public void onStart() {
                                        logMessage("Event: start");
                                    }

                                    @Override
                                    public void onStop() {
                                        logMessage("Event: end");
                                    }
                                })
                                .setErrorCallback(throwable -> {
                                    logError(throwable, "Subscribed to PlayerContext failed!");
                                });
                        // Now you can start interacting with App Remote
//                        connected();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });


    }

    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
//        onDisconnected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            String mode = "";
            if (sectionNumber == 1) {
                mode = "STEADY";
            } else if (sectionNumber == 2) {
                mode = "RANDOM";
            } else if (sectionNumber == 3) {
                mode = "RAMP UP";
            } else if (sectionNumber == 4) {
                mode = "RAMP DOWN";
            }
            ToSkipToWait skippingInfo = modeMap.get(mode);
            args.putString(ARG_SECTION_NUMBER, mode);
            args.putSerializable(mode, skippingInfo);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            String mode = getArguments().getString(ARG_SECTION_NUMBER);
            ToSkipToWait skipInfo = (ToSkipToWait) getArguments().getSerializable(mode);
            View rootView = null;


            final TextView infoOn;
            final TextView counts;
            if (mode.equals("STEADY")) {
                rootView = inflater.inflate(R.layout.steady_page, container, false);
                counts = rootView.findViewById(R.id.countSteady);
                infoOn = rootView.findViewById(R.id.infoSteady);

                //intialize buttons
                Button maxTimeBt = (Button) rootView.findViewById(R.id.maxSteady);
                Button numTillSkipBt = (Button) rootView.findViewById(R.id.numSongSteady);
                Button minusBt = (Button) rootView.findViewById(R.id.minusSteady);
                Button plusBt = (Button) rootView.findViewById(R.id.plusSteady);
                maxTimeBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        counts.setText(skipInfo.getMaxWaitTime() + "");
                        infoOn.setText("Change duration of pause between each track.");
                        maxTimeBt.setTypeface(Typeface.DEFAULT_BOLD);
                        numTillSkipBt.setTypeface(Typeface.DEFAULT);
                        skipInfo.setChangeState(ToSkipToWait.ChangeState.CHG_MAX_TIME);
                    }
                });
                numTillSkipBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        counts.setText(skipInfo.getNumToSkipWait() + "");
                        infoOn.setText("Change the number of song to play before pause.");
                        maxTimeBt.setTypeface(Typeface.DEFAULT);
                        numTillSkipBt.setTypeface(Typeface.DEFAULT_BOLD);
                        skipInfo.setChangeState(ToSkipToWait.ChangeState.CHG_NUM_TILL_SKIP);

                    }
                });
                minusBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        skipInfo.decBaseOnChangeState();
                        if(skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_MAX_TIME){
                            counts.setText(skipInfo.getMaxWaitTime() + "");

                        }else{
                            counts.setText(skipInfo.getNumToSkipWait() + "");

                        }
                    }
                });
                plusBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        skipInfo.incBaseOnChangeState();
                        if(skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_MAX_TIME){
                            counts.setText(skipInfo.getMaxWaitTime() + "");

                        }else{
                            counts.setText(skipInfo.getNumToSkipWait() + "");

                        }
                    }
                });

            } else if (mode.equals("RANDOM")) {
                rootView = inflater.inflate(R.layout.random_page, container, false);
                counts = rootView.findViewById(R.id.countRandom);
                infoOn = rootView.findViewById(R.id.infoRandom);

                Button maxTimeBt = (Button) rootView.findViewById(R.id.maxRandom);
                Button minTimeBt = (Button) rootView.findViewById(R.id.minRandom);
                Button numTillSkipBt = (Button) rootView.findViewById(R.id.numSongRandom);
                Button minusBt = (Button) rootView.findViewById(R.id.minusRandom);
                Button plusBt = (Button) rootView.findViewById(R.id.plusRandom);

                maxTimeBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        counts.setText(skipInfo.getMaxWaitTime() + "");
                        infoOn.setText("Change max duration of pause between each track.");
                        maxTimeBt.setTypeface(Typeface.DEFAULT_BOLD);
                        numTillSkipBt.setTypeface(Typeface.DEFAULT);
                        minTimeBt.setTypeface(Typeface.DEFAULT);
                        skipInfo.setChangeState(ToSkipToWait.ChangeState.CHG_MAX_TIME);
                    }
                });
                minTimeBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        counts.setText(skipInfo.getMinWaitTime() + "");
                        infoOn.setText("Change min duration of pause between each track.");
                        maxTimeBt.setTypeface(Typeface.DEFAULT);
                        numTillSkipBt.setTypeface(Typeface.DEFAULT);
                        minTimeBt.setTypeface(Typeface.DEFAULT_BOLD);
                        skipInfo.setChangeState(ToSkipToWait.ChangeState.CHG_MIN_TIME);
                    }
                });
                numTillSkipBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        counts.setText(skipInfo.getNumToSkipWait() + "");
                        infoOn.setText("Change the number of song to play before pause.");
                        maxTimeBt.setTypeface(Typeface.DEFAULT);
                        numTillSkipBt.setTypeface(Typeface.DEFAULT_BOLD);
                        minTimeBt.setTypeface(Typeface.DEFAULT);
                        skipInfo.setChangeState(ToSkipToWait.ChangeState.CHG_NUM_TILL_SKIP);
                    }
                });
                minusBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        skipInfo.decBaseOnChangeState();
                        if(skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_MAX_TIME){
                            counts.setText(skipInfo.getMaxWaitTime() + "");

                        } else if (skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_MIN_TIME){
                            counts.setText(skipInfo.getMinWaitTime() + "");

                        }else if (skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_NUM_TILL_SKIP){
                            counts.setText(skipInfo.getNumToSkipWait() + "");
                        }

                    }
                });
                plusBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        skipInfo.incBaseOnChangeState();
                        if(skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_MAX_TIME){
                            counts.setText(skipInfo.getMaxWaitTime() + "");

                        } else if (skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_MIN_TIME){
                            counts.setText(skipInfo.getMinWaitTime() + "");

                        }else if (skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_NUM_TILL_SKIP){
                            counts.setText(skipInfo.getNumToSkipWait() + "");
                        }
                    }
                });
            } else if (mode.equals("RAMP UP")) {
                rootView = inflater.inflate(R.layout.ramp_up_page, container, false);
                counts = rootView.findViewById(R.id.countRampUp);
                infoOn = rootView.findViewById(R.id.infoRampUp);
                Button maxTimeBt = (Button) rootView.findViewById(R.id.maxRampUp);
                Button stepBt = (Button) rootView.findViewById(R.id.stepRampUp);
                Button numTillSkipBt = (Button) rootView.findViewById(R.id.numSongRampUp);
                Button minusBt = (Button) rootView.findViewById(R.id.minusRampUp);
                Button plusBt = (Button) rootView.findViewById(R.id.plusRampUp);


                maxTimeBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        counts.setText(skipInfo.getMaxWaitTime() + "");
                        infoOn.setText("Change max duration of pause between each track.");
                        maxTimeBt.setTypeface(Typeface.DEFAULT_BOLD);
                        numTillSkipBt.setTypeface(Typeface.DEFAULT);
                        stepBt.setTypeface(Typeface.DEFAULT);
                        skipInfo.setChangeState(ToSkipToWait.ChangeState.CHG_MAX_TIME);
                    }
                });
                stepBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        counts.setText(skipInfo.getChangeStep() + "");
                        infoOn.setText("Change rate of change duration to pause between each track.");
                        maxTimeBt.setTypeface(Typeface.DEFAULT);
                        numTillSkipBt.setTypeface(Typeface.DEFAULT);
                        stepBt.setTypeface(Typeface.DEFAULT_BOLD);
                        skipInfo.setChangeState(ToSkipToWait.ChangeState.CHG_STEP);
                    }
                });
                numTillSkipBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        counts.setText(skipInfo.getNumToSkipWait() + "");
                        infoOn.setText("Change the number of song to play before pause.");
                        maxTimeBt.setTypeface(Typeface.DEFAULT);
                        numTillSkipBt.setTypeface(Typeface.DEFAULT_BOLD);
                        stepBt.setTypeface(Typeface.DEFAULT);
                        skipInfo.setChangeState(ToSkipToWait.ChangeState.CHG_NUM_TILL_SKIP);
                    }
                });
                minusBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        skipInfo.decBaseOnChangeState();
                        if(skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_MAX_TIME){
                            counts.setText(skipInfo.getMaxWaitTime() + "");

                        } else if (skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_MIN_TIME){
                            counts.setText(skipInfo.getMinWaitTime() + "");

                        }else if (skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_NUM_TILL_SKIP){
                            counts.setText(skipInfo.getNumToSkipWait() + "");
                        }else if (skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_STEP){
                            counts.setText(skipInfo.getChangeStep() + "");
                        }

                    }
                });
                plusBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        skipInfo.incBaseOnChangeState();
                        if(skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_MAX_TIME){
                            counts.setText(skipInfo.getMaxWaitTime() + "");

                        } else if (skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_MIN_TIME){
                            counts.setText(skipInfo.getMinWaitTime() + "");

                        }else if (skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_NUM_TILL_SKIP){
                            counts.setText(skipInfo.getNumToSkipWait() + "");
                        }else if (skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_STEP){
                            counts.setText(skipInfo.getChangeStep() + "");
                        }
                    }
                });
            } else if (mode.equals("RAMP DOWN")) {
                rootView = inflater.inflate(R.layout.ramp_down_page, container, false);
                counts = rootView.findViewById(R.id.countRampDown);
                infoOn = rootView.findViewById(R.id.infoRampDown);
                Button maxTimeBt = (Button) rootView.findViewById(R.id.maxRampDown);
                Button stepBt = (Button) rootView.findViewById(R.id.stepRampDown);
                Button numTillSkipBt = (Button) rootView.findViewById(R.id.numSongRampDown);
                Button minusBt = (Button) rootView.findViewById(R.id.minusRampDown);
                Button plusBt = (Button) rootView.findViewById(R.id.plusRampDown);

                maxTimeBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        counts.setText(skipInfo.getMaxWaitTime() + "");
                        infoOn.setText("Change max duration of pause between each track.");
                        maxTimeBt.setTypeface(Typeface.DEFAULT_BOLD);
                        numTillSkipBt.setTypeface(Typeface.DEFAULT);
                        stepBt.setTypeface(Typeface.DEFAULT);
                        skipInfo.setChangeState(ToSkipToWait.ChangeState.CHG_MAX_TIME);
                    }
                });
                stepBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        counts.setText(skipInfo.getChangeStep() + "");
                        infoOn.setText("Change rate of change duration to pause between each track.");
                        maxTimeBt.setTypeface(Typeface.DEFAULT);
                        numTillSkipBt.setTypeface(Typeface.DEFAULT);
                        stepBt.setTypeface(Typeface.DEFAULT_BOLD);
                        skipInfo.setChangeState(ToSkipToWait.ChangeState.CHG_STEP);
                    }
                });
                numTillSkipBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        counts.setText(skipInfo.getNumToSkipWait() + "");
                        infoOn.setText("Change the number of song to play before pause.");
                        maxTimeBt.setTypeface(Typeface.DEFAULT);
                        numTillSkipBt.setTypeface(Typeface.DEFAULT_BOLD);
                        stepBt.setTypeface(Typeface.DEFAULT);
                        skipInfo.setChangeState(ToSkipToWait.ChangeState.CHG_NUM_TILL_SKIP);
                    }
                });
                minusBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        skipInfo.decBaseOnChangeState();
                        if(skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_MAX_TIME){
                            counts.setText(skipInfo.getMaxWaitTime() + "");

                        } else if (skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_MIN_TIME){
                            counts.setText(skipInfo.getMinWaitTime() + "");

                        }else if (skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_NUM_TILL_SKIP){
                            counts.setText(skipInfo.getNumToSkipWait() + "");
                        }else if (skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_STEP){
                            counts.setText(skipInfo.getChangeStep() + "");
                        }
                    }
                });
                plusBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        skipInfo.incBaseOnChangeState();
                        if(skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_MAX_TIME){
                            counts.setText(skipInfo.getMaxWaitTime() + "");

                        } else if (skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_MIN_TIME){
                            counts.setText(skipInfo.getMinWaitTime() + "");

                        }else if (skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_NUM_TILL_SKIP){
                            counts.setText(skipInfo.getNumToSkipWait() + "");
                        }else if (skipInfo.getChangeState() == ToSkipToWait.ChangeState.CHG_STEP){
                            counts.setText(skipInfo.getChangeStep() + "");
                        }
                    }
                });
            }


//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.section_format, mode));
            return rootView;
        }


    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }
    }

    public void onPlayPauseButtonClicked(View view) {
        mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
            if (playerState.isPaused) {
                mSpotifyAppRemote.getPlayerApi()
                        .resume()
                        .setResultCallback(empty -> logMessage("Play current track successful"))
                        .setErrorCallback(mErrorCallback);
            } else {
                mSpotifyAppRemote.getPlayerApi()
                        .pause()
                        .setResultCallback(empty -> logMessage("Pause successful"))
                        .setErrorCallback(mErrorCallback);
            }
        });
    }

    public void onSkipPreviousButtonClicked(View view) {
        mSpotifyAppRemote.getPlayerApi()
                .skipPrevious()
                .setResultCallback(empty -> logMessage("Skip previous successful"))
                .setErrorCallback(mErrorCallback);
    }

    public void onSkipNextButtonClicked(View view) {
        mSpotifyAppRemote.getPlayerApi()
                .skipNext()
                .setResultCallback(data -> {
                    logMessage("Skip next successful");
                })
                .setErrorCallback(mErrorCallback);
    }

    private void logMessage(String msg) {
        logMessage(msg, Toast.LENGTH_SHORT);
    }

    private void logMessage(String msg, int duration) {
        Toast.makeText(this, msg, duration).show();
        Log.d(TAG, msg);
    }

    private void logError(Throwable throwable, String msg) {
        Toast.makeText(this, "Error: " + msg, Toast.LENGTH_SHORT).show();
        Log.e(TAG, msg, throwable);
    }
}
