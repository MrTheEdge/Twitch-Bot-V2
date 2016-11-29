package com.github.mrtheedge.twitchbot;

import com.github.mrtheedge.twitchbot.exceptions.NoSuchCommandException;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by E.J. Schroeder on 11/25/2016.
 *
 * Manages the built in and custom commands for the bot. All the CRUD operations.
 */
public class CommandManager {

    private Map<String, Command> commandMap;
    private UserDataManager dataSource;
    private final Pattern VARIABLE_PATTERN = Pattern.compile("<(\\w+)(\\(.*\\))?>");

    public CommandManager() {
        commandMap = new HashMap<>();

    }

    public void setDataSource(UserDataManager source){
        dataSource = source;
    }

    /*
        First word of the string will be the command name
        It should be passed without the ! attached to the front
     */
    public String parseCommand(String line, ImmutableMap<String, String> tags) throws NoSuchCommandException {
        Iterator<String> splitLine = Splitter.on(" ").omitEmptyStrings().split(line).iterator();

        String commandName;
        if (splitLine.hasNext()){
            commandName = splitLine.next();
        } else {
            throw new NoSuchCommandException();
        }

        List<String> args = new ArrayList<>();
        splitLine.forEachRemaining(args::add);

        String output;

        /*
            https://docs.oracle.com/javase/7/docs/api/java/util/regex/Matcher.html#appendReplacement(java.lang.StringBuffer,%20java.lang.String)
            For replacing variables
        */

        if (commandMap.containsKey(commandName)){
            Command command = commandMap.get(commandName);
            // Custom command was found

            output = command.callCommand();
        } else {
            // Look for built in command

            /* Built-ins:
               - Add        -> !addcom {name} [permissions] {content}
               - Edit       -> !editcom {name}
               - Delete     -> !delcom
               - Raffle     -> !raffle
               - Auction    -> !auction
               - Blacklist  -> !blacklist {add|del} {word}
               - Poll       -> !poll [title|option|open] {content}
               - Vote       -> !vote {number}
               - Timers      -> !timers {add|del} {name} [minutes] [Content content content]
             */

            output = checkForBuiltIn(commandName, args);
        }

        return "";
    }

    private String checkForBuiltIn(String commandName, List<String> args) {

        switch (commandName){
            case "addcom":
                return parseAddcom(args);
                break;
            case "editcom":
                return parseEditcom(args);
                break;
            case "delcom":
                return parseDelcom(args);
                break;
            case "raffle":
                return parseRaffle(args);
                break;
            case "auction":
                return parseAuction(args);
                break;
            case "blacklist":
                return parseBlacklist(args);
                break;
            case "poll":
                return parsePoll(args);
                break;
            case "vote":
                return parseVote(args);
                break;
            case "timers":
                return parseTimers(args);
                break;
            default:
                return "";
        }
    }

}
