package com.musicspigot.musicspigot;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;

import java.util.Timer;
import java.util.TimerTask;

public class ToSkipToPauseService extends Service {



    public ToSkipToPauseService() {
    }
    private static final String TAG = "ToSkipToPauseService";
    public static SpotifyAppRemote mSpotifyAppRemote;
    private static final String CLIENT_ID = "28f294f65e454bc39194baf504a1a4da";
    private static final String REDIRECT_URI = "MusicSpigot://callback";
    Subscription<PlayerState> mPlayerStateSubscription;
    Timer timer = new Timer();
    private String name = "";



    private final Subscription.EventCallback<PlayerState> mPlayerStateEventCallback = new Subscription.EventCallback<PlayerState>() {
        @Override
        public void onEvent(PlayerState playerState) {

            Log.i(TAG + "  " + playerState.track.name, ": my playerstart: " + playerState.track.duration + ": " + playerState.playbackPosition);
            //if pausing active
            if(MainActivity.active) {
                //if we get a track name change
                if (!name.equals(playerState.track.name)) {
                    ToSkipToWait skipObj =null;
                    switch (MainActivity.activeOpMode){

                        case STEADY:
                            skipObj=MainActivity.modeMap.get("STEADY");
                            break;
                        case RANDOM:
                            skipObj=MainActivity.modeMap.get("RANDOM");
                            break;
                        case RAMP_UP:
                            skipObj=MainActivity.modeMap.get("RAMP UP");
                            break;
                        case RAMP_DOWN:
                            skipObj=MainActivity.modeMap.get("RAMP DOWN");
                            break;
                        case DISABLED:
                            break;
                    }
                    Log.i(TAG + "  " + playerState.track.name, ": my playerstart: " + playerState.track.duration + ": " + playerState.playbackPosition);
                    if (skipObj != null && skipObj.toWait()) {
                        mSpotifyAppRemote.getPlayerApi().pause();
                        //schedule wait to play
                        if(MainActivity.currTimerTask != null){
                            MainActivity.currTimerTask.cancel();
                        }
                        TimerTask resumePlay = new ResumePlayTask(mSpotifyAppRemote.getPlayerApi());
                        MainActivity.currTimerTask  = resumePlay;
                        //multiply by 1000 to get milli-seconds
                        timer.schedule(resumePlay, skipObj.getWaitTimeInSeconds() * 1000);
                        Log.i(TAG + "  " + playerState.track.name, skipObj.toString());

                    }
                    name = playerState.track.name;
                }
            }

        }
    };
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();
        //start sticky means service will be explicity started and stopped
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
        return START_STICKY;
    }


    private void logMessage(String msg) {
        logMessage(msg, Toast.LENGTH_SHORT);
    }

    private void logMessage(String msg, int duration) {
        Toast.makeText(this, msg, duration).show();
        Log.d("HI", msg);
    }

    private void logError(Throwable throwable, String msg) {
        Toast.makeText(this, "Error: " + msg, Toast.LENGTH_SHORT).show();
        Log.e("HI", msg, throwable);
    }

}
