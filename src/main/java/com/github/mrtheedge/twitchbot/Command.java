package com.github.mrtheedge.twitchbot;

/**
 * Created by E.J. Schroeder on 11/15/2016.
 */
public abstract class Command {

    private String name;
    private UserType userType;
    private int requiredParams;
    private String content;
    private int cooldown;
    private String cooldownMessage;

    private long lastUseTimestamp;
    private int useCount;
    private boolean messageDuringCooldown;

    public Command(String name, UserType userType, String content) {
        this.name = name;
        this.userType = userType;
        this.content = content;
    }


    enum UserType {
        Default, Subscriber, Mod, Broadcaster
    }

}

