package com.github.mrtheedge.twitchbot;

import com.github.mrtheedge.twitchbot.exceptions.CommandOnCooldownException;
import com.github.mrtheedge.twitchbot.exceptions.InsufficientPermissionException;

import java.time.Instant;

/**
 * Created by E.J. Schroeder on 11/15/2016.
 *
 * Holds information about a custom command. Each command has a permission level, the actual content, along with some
 * other options like the cost of using the command and if it has a cool down period.
 */
public class Command {

    protected String name;
    protected PermissionLevel permissionLevel;
    protected String content;
    protected int cooldown;
    protected int pointCost;

    private long lastUseTimestamp;
    private int useCount;

    public Command(String name, PermissionLevel permissionLevel, String content) {
        this.name = name;
        this.permissionLevel = permissionLevel;
        this.content = content;
        this.cooldown = 0;
        this.pointCost = 0;

        lastUseTimestamp = 0;
        useCount = 0;
    }

    public boolean isCallableBy(PermissionLevel level) {
        return level.ordinal() >= permissionLevel.ordinal();
    }

    public String callCommand(PermissionLevel level) throws InsufficientPermissionException, CommandOnCooldownException {

        long currentTime = Instant.now().getEpochSecond();

        if ( !isCallableAt(currentTime) ){
            throw new CommandOnCooldownException();
        }

        if (!isCallableBy(level)){
            throw new InsufficientPermissionException();
        }

        lastUseTimestamp = currentTime;
        useCount++;

        return content;
    }

    public boolean isOffCooldown(){
        return isCallableAt(Instant.now().getEpochSecond());
    }

    private boolean isCallableAt(long time){
        return time - lastUseTimestamp > cooldown;
    }

    public void setCooldown(int seconds){
        if (seconds >= 0)
            cooldown = seconds;
    }

    public void setPointCost(int amount){
        if (amount >= 0)
            pointCost = amount;
    }

    public int getUseCount(){
        return useCount;
    }

    public int getUseCost(){
        return pointCost;
    }

}

enum PermissionLevel {
    None, Subscriber, Mod, Broadcaster
}
