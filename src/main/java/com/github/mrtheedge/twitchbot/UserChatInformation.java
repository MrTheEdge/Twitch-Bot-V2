package com.github.mrtheedge.twitchbot;

import java.time.Instant;

/**
 * Created by E.J. Schroeder on 11/18/2016.
 *
 * Data structure for storing various timestamps for each user. The last message time is used to determine if the user
 * is active in chat. Also stores when the user first started viewing, along with the total view time as a duration.
 *
 * When creating a new Information object for a user, it is assumed that the creation means that the user joined the chat.
 * There is no need to call joinChat() if the object has just been created.
 */
public class UserChatInformation {

    private long lastMessageAt; // For determining if user is "active" having sent a message within last X minutes.
    private final long createdAt; // Used to find total time a user has been a viewer
    private long viewDuration; // Total viewing time while the bot is in chat
    private transient long viewSessionStartTime;
    private transient boolean isInChat = false;
    private int currencyAmt;

    public UserChatInformation(){
        createdAt = Instant.now().getEpochSecond();
        currencyAmt = 0;
    }

    public boolean isInChat(){
        return isInChat;
    }

    public long getCreatedAt(){
        return createdAt;
    }

    public void updateLastMessageTime(){
        lastMessageAt = Instant.now().getEpochSecond();
    }

    public long getLastMessageTime() {
        return lastMessageAt;
    }

    public long getViewDuration(){
        if (isInChat) {
            updateViewDuration();
        }

        return viewDuration;
    }

    public void updateViewDuration(){
        long currentTime = Instant.now().getEpochSecond();

        if (viewSessionStartTime > 0){ // Make sure the last view session has a start time, so the view duration doesn't become years big
            long sessionDuration = currentTime - viewSessionStartTime;
            viewDuration += sessionDuration;
        }

        viewSessionStartTime = currentTime;
    }

    public int removeCurrency(int amount){
        currencyAmt -= amount;
        if (currencyAmt < 0) currencyAmt = 0;
        return currencyAmt;
    }

    public int addCurrency(int amount){
        currencyAmt += amount;
        return currencyAmt;
    }

    public int getAmountOfCurrency(){
        return currencyAmt;
    }

    public void joinChat(){
        isInChat = true;
        viewSessionStartTime = Instant.now().getEpochSecond();
    }

    public void leaveChat(){
        isInChat = false;
        updateViewDuration();       // Calculate the final duration that the user was in chat
        viewSessionStartTime = 0;   // Set the session start time to 0, since it will be reset upon joining.
    }

}
