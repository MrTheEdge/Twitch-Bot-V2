package com.github.mrtheedge.twitchbot;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by E.J. Schroeder on 11/17/2016.
 *
 * Reviews a message and determines if it meets the criteria of a spam message.
 *
 */
public class SpamFilter {


    private Map<String, Integer> userStrikes;   // Stores the number of strikes a user has from other offenses
    private Set<String> pardonedUsers;          // Stores the names of users that have a pass to post something that would be marked as spam.
    private Set<String> blacklist;
    private StrikeCallback strikeCallback;

    private SimpleIntegerProperty allowedStrikes;                 // The number of strikes before a user is timed out/banned
    private SimpleDoubleProperty percentageCaps;              // Percentage of capital letters allowed in a message.
    private SimpleIntegerProperty minimumWordLengthForCaps;
    private SimpleIntegerProperty timeoutSeconds;
    private final Pattern LINK_REGEX = Pattern.compile("(https?://)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([/\\w \\.-]*)*/?");

    private SimpleBooleanProperty checkForCaps;
    private SimpleBooleanProperty checkForLinks;
    private SimpleBooleanProperty checkBlacklist;
    private SimpleBooleanProperty allowPardons;
    private SimpleBooleanProperty timeoutOnStrikes;

    public SpamFilter(){
        userStrikes = new HashMap<>();
        pardonedUsers = new HashSet<>();
        blacklist = new HashSet<>();

        allowedStrikes = new SimpleIntegerProperty(3);
        percentageCaps = new SimpleDoubleProperty(0.75);
        minimumWordLengthForCaps = new SimpleIntegerProperty(5);
        timeoutSeconds = new SimpleIntegerProperty(15 * 60); // 15 minutes

        checkForCaps = new SimpleBooleanProperty(true);
        checkForLinks = new SimpleBooleanProperty(true);
        checkBlacklist = new SimpleBooleanProperty(true);
        allowPardons = new SimpleBooleanProperty(true);
        timeoutOnStrikes = new SimpleBooleanProperty(true);
    }

    public SpamType isSpam(String nick, String message){

        if (checkForCaps.getValue() && messageExceedsCapsLimit(message)){
            if (pardonedUsers.remove(nick)) // If the user has been pardoned, ignore the spam
                return SpamType.NONE;

            addStrikeToUser(nick);
            return SpamType.CAPS;
        }
        if (checkForLinks.getValue() && messageContainsLink(message)){
            if (pardonedUsers.remove(nick)) // If the user has been pardoned, ignore the spam
                return SpamType.NONE;

            addStrikeToUser(nick);
            return SpamType.LINK;
        }
        if (checkBlacklist.getValue() && messageContainsBlacklistedWord(message)){
            if (pardonedUsers.remove(nick)) // If the user has been pardoned, ignore the spam
                return SpamType.NONE;

            addStrikeToUser(nick);
            return SpamType.BLACKLISTED;
        }

        return SpamType.NONE; // If the message gets through the checks, it's not spam
    }

    public void pardonUser(String user){
        if (allowPardons.getValue())
            pardonedUsers.add(user);
    }

    public void addWordToBlacklist(String word){
        blacklist.add(word);
    }

    public void removeWordFromBlacklist(String word){
        blacklist.remove(word);
    }

    public void registerCallback(StrikeCallback strikeCallback){
        this.strikeCallback = strikeCallback;
    }

    private void addStrikeToUser(String user){
        Integer strikes = userStrikes.get(user); // Could be null

        strikes = strikes == null ? 1 : strikes + 1;

        if (strikes >= allowedStrikes.getValue()){
            // Call strikeCallback to ban/time out user...
            userStrikes.remove(user);
            if (strikeCallback != null && timeoutOnStrikes.getValue()) strikeCallback.call(user, timeoutSeconds.getValue());
        } else {
            userStrikes.put(user, strikes); // Otherwise, update the users strike count to new value
        }
    }

    private boolean messageContainsBlacklistedWord(String message){ // Could probably use some improvement, but...
        String[] words = message.split(" ");
        for (String w : words){
            if (blacklist.contains(w)) return true;
        }
        return false;
    }

    private boolean messageExceedsCapsLimit(String message){

        int msgLength = message.length();
        int capsCount = 0;
        double percentage;

        if ( msgLength > minimumWordLengthForCaps.getValue()){

            // Counting number of capital characters
            for (int i = 0; i < msgLength; i++) {
                if (Character.isUpperCase(message.charAt(i))) capsCount++;
            }

            percentage = ((double) capsCount) / msgLength; // Calculate caps percentage

            if (percentage > percentageCaps.getValue())
                return true;
        }
        return false;

    }

    private boolean messageContainsLink(String message){
        return LINK_REGEX.matcher(message).find();
    }
}

enum SpamType {
    NONE, CAPS, LINK, BLACKLISTED
}