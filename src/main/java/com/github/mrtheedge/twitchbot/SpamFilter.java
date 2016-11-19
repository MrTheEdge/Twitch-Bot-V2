package com.github.mrtheedge.twitchbot;

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

    private int allowedStrikes;                // The number of strikes before a user is timed out/banned
    private double percentageCaps;              // Percentage of capital letters allowed in a message.
    private int minimumWordLengthForCaps;
    private int timeoutSeconds;
    private final Pattern LINK_REGEX = Pattern.compile("(https?://)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([/\\w \\.-]*)*/?");

    private boolean checkForCaps;
    private boolean checkForLinks;
    private boolean checkBlacklist;
    private boolean allowPardons;
    private boolean timeoutOnStrikes;

    public SpamFilter(){
        userStrikes = new HashMap<String, Integer>();
        pardonedUsers = new HashSet<String>();
        blacklist = new HashSet<String>();

        allowedStrikes = 3;
        percentageCaps = 0.75;
        minimumWordLengthForCaps = 5;
        timeoutSeconds = 15 * 60; // 15 minutes

        checkForCaps = true;
        checkForLinks = true;
        checkBlacklist = true;
        allowPardons = true;
        timeoutOnStrikes = true;
    }

    public SpamType isSpam(String nick, String message){

        if (checkForCaps && messageExceedsCapsLimit(message)){
            if (pardonedUsers.remove(nick)) // If the user has been pardoned, ignore the spam
                return SpamType.NONE;

            addStrikeToUser(nick);
            return SpamType.CAPS;
        }
        if (checkForLinks && messageContainsLink(message)){
            if (pardonedUsers.remove(nick)) // If the user has been pardoned, ignore the spam
                return SpamType.NONE;

            addStrikeToUser(nick);
            return SpamType.LINK;
        }
        if (checkBlacklist && messageContainsBlacklistedWord(message)){
            if (pardonedUsers.remove(nick)) // If the user has been pardoned, ignore the spam
                return SpamType.NONE;

            addStrikeToUser(nick);
            return SpamType.BLACKLISTED;
        }

        return SpamType.NONE; // If the message gets through the checks, it's not spam
    }

    public void pardonUser(String user){
        if (allowPardons)
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

        if (strikes >= allowedStrikes){
            // Call strikeCallback to ban/time out user...
            userStrikes.remove(user);
            if (strikeCallback != null && timeoutOnStrikes) strikeCallback.call(user, timeoutSeconds);
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

        if ( msgLength > minimumWordLengthForCaps){

            // Counting number of capital characters
            for (int i = 0; i < msgLength; i++) {
                if (Character.isUpperCase(message.charAt(i))) capsCount++;
            }

            percentage = ((double) capsCount) / msgLength; // Calculate caps percentage

            if (percentage > percentageCaps)
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