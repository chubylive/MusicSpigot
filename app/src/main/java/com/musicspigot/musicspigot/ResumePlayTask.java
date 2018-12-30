package com.musicspigot.musicspigot;

import com.spotify.android.appremote.api.PlayerApi;

import java.util.TimerTask;

public class ResumePlayTask extends TimerTask {
    PlayerApi playerApi;

    public ResumePlayTask (PlayerApi playerApi){
        this.playerApi = playerApi;
    }

    public void run (){
        playerApi.resume();
    }
}
