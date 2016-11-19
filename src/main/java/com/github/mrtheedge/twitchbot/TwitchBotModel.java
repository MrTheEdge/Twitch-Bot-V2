package com.github.mrtheedge.twitchbot;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.cap.EnableCapHandler;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;

import java.io.IOException;
import java.util.Map;

/**
 * Created by E.J. Schroeder on 11/15/2016.
 *
 * The main model behind the application. Also functions as a listener for the actual IRC bot.
 */
public class TwitchBotModel extends ListenerAdapter {

    private SpamFilter sf;
    private PircBotX bot;
    private String channel;
    private String username;
    private String oauth;

    private Map<String, UserTimestamps> users;

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
    public void onJoin(JoinEvent event) throws Exception {

    }

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        sf.isSpam(event.getUser().getNick(), event.getMessage());

        if (event.getMessage().equals("!disconnect")){
            bot.send().quitServer();
        }
    }

    @Override
    public void onNotice(NoticeEvent event) throws Exception {

    }

    @Override
    public void onOp(OpEvent event) throws Exception {

    }

    @Override
    public void onPart(PartEvent event) throws Exception {

    }

    @Override
    public void onPrivateMessage(PrivateMessageEvent event) throws Exception {

    }

    @Override
    public void onUnknown(UnknownEvent event) throws Exception {

    }

    public void connect() {
        try {
            bot.startBot();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IrcException e) {
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
                .setName("BOT_NAME_HERE") //Your twitch.tv username
                .setServerPassword("oauth:TOKEN_HERE") //Your oauth password from http://twitchapps.com/tmi
                .addAutoJoinChannel("#CHANNEL_NAME_HERE") //Some twitch channel
                .addListener(this)
                .buildConfiguration();

        bot = new PircBotX(config);

        sf = new SpamFilter();
        sf.registerCallback((u, t) -> {
            bot.send().message("#CHANNEL_NAME_HERE", "/timeout " + u + " " + t);
        });
    }

    public static void main(String[] args){
        TwitchBotModel tbm = new TwitchBotModel();
        tbm.connect();
    }
}
