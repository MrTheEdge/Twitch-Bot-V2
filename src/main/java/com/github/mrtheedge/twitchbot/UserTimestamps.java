package com.github.mrtheedge.twitchbot;

import java.time.Instant;

/**
 * Created by E.J. Schroeder on 11/18/2016.
 */
public class UserTimestamps {

    private long lastMessageAt;
    private final long createdAt;
    private long viewDuration;
    private boolean isInChat = false;

    public UserTimestamps(){
        createdAt = Instant.now().getEpochSecond();
    }

    public long getCreatedAt(){ return createdAt; }

    public void updateLastMessageTime(long newTime){
        if (newTime > lastMessageAt) lastMessageAt = newTime;
    }

    public long getLastMessageTime() { return lastMessageAt; }

    public void updateViewDuration() {



    }

}
