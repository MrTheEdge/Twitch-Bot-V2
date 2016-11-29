package com.github.mrtheedge.twitchbot;

import com.github.mrtheedge.twitchbot.exceptions.NoSuchCommandException;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.cap.EnableCapHandler;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by E.J. Schroeder on 11/15/2016.
 *
 * The main model behind the application. Also functions as a listener for the actual IRC bot.
 */
public class TwitchBotModel extends ListenerAdapter {

    private SpamFilter spamFilter;
    private UserDataManager userDataManager;
    private CommandManager commandManager;
    private PircBotX bot;
    private String channel = "YOUR_CHANNEL_NAME";
    private String username = "BOT_NAME";
    private String oauth = "OAUTH_TOKEN";

    private Map<String, UserChatInformation> users;
    private Map<String, Command> commands;

    // https://tmi.twitch.tv/group/user/CHANNELNAME/chatters
    // Use this to get the initial list of viewers when we enter chat...


    @Override
    public void onConnect(ConnectEvent event) throws Exception {

    }

    @Override
    public void onConnectAttemptFailed(ConnectAttemptFailedEvent event) throws Exception {

    }

    @Override
    public void onDisconnect(DisconnectEvent event) throws Exception {

    }

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        User user = event.getUser();
        if (user == null) return;

        if (event.getMessage().equals("!disconnect")){
            // Only the broadcaster should be able to disconnect
            if (user.getNick().equals(channel.substring(1))) {
                bot.send().quitServer();
                return;
            }
        }

        userDataManager.newMessage(user.getNick()); // Add the latest messages timestamp for the user

        String commandResponse = "";
        if (event.getMessage().startsWith("!")){
            String trimmedLine = event.getMessage().substring(1); // Trim off the '!'
            try {
                commandResponse = commandManager.parseCommand(trimmedLine, event.getTags());
            } catch (NoSuchCommandException ex){
                ex.printStackTrace();
            }
        }

        if (commandResponse.equals("")){
            // Either no command or the command was invalid. Prevents bypassing the spam filter with a '!'
            SpamType type = spamFilter.isSpam(user.getNick(), event.getMessage());
        }
    }

    @Override
    public void onNotice(NoticeEvent event) throws Exception {

    }

    @Override
    public void onJoin(JoinEvent event) throws Exception {
        User u = event.getUser();
        if (u == null) return;

        userDataManager.join(u.getNick());
    }

    @Override
    public void onPart(PartEvent event) throws Exception {
        User u = event.getUser();
        if (u == null) return;

        userDataManager.part(u.getNick());
    }

    @Override
    public void onPrivateMessage(PrivateMessageEvent event) throws Exception {
        // TODO Eventually add whisper support for the bot...
    }

    @Override
    public void onUnknown(UnknownEvent event) throws Exception {
        //
    }

    public void connect() {
        try {
            bot.startBot();
        } catch (IOException | IrcException e) {
            e.printStackTrace();
        }
    }

    public TwitchBotModel(){
        Configuration config = new Configuration.Builder()
                .setAutoNickChange(false) //Twitch doesn't support multiple users
                .setOnJoinWhoEnabled(false) //Twitch doesn't support WHO command
                .setCapEnabled(true)
                .addCapHandler(new EnableCapHandler("twitch.tv/membership")) //Twitch by default doesn't send JOIN, PART, and NAMES unless you request it, see https://github.com/justintv/Twitch-API/blob/master/IRC.md#membership
                .addCapHandler(new EnableCapHandler("twitch.tv/tags"))
                .addCapHandler(new EnableCapHandler("twitch.tv/commands"))
                .addServer("irc.twitch.tv")
                .setName(username) //Your twitch.tv username
                .setServerPassword(oauth) //Your oauth password from http://twitchapps.com/tmi
                .addAutoJoinChannel(channel) //Some twitch channel
                .addListener(this)
                .buildConfiguration();

        bot = new PircBotX(config);

        spamFilter = new SpamFilter();
        userDataManager = new UserDataManager();
        commands = new ConcurrentHashMap<>();
        spamFilter.registerCallback((u, t) -> bot.send().message(channel, "/timeout " + u + " " + t));
    }

    public static void main(String[] args){
        TwitchBotModel tbm = new TwitchBotModel();
        tbm.connect();
    }
}
