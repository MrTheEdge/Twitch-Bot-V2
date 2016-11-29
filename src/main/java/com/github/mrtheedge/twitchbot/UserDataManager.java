package com.github.mrtheedge.twitchbot;

import com.github.mrtheedge.twitchbot.exceptions.NoSuchUserException;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by E.J. Schroeder on 11/23/2016.
 *
 * Stores data for each chat user. This includes when the bot first saw the user, how long the user has spent viewing the
 * stream, their last message time, and whether they are considered an active chatter.  Also allows for getting data
 * such as who has the most time viewing, who has the most currency, or the top N people in a category.
 */
public class UserDataManager {

    private Map<String, UserChatInformation> users;
    private List<String> usersInChat;
    private final Object lock = new Object();

    // https://blog.simplypatrick.com/til/2016/2016-03-02-post-processing-GSON-deserialization/
    // Information so that on de-serialization, we can call a method that re-loads the sortedUsers list without having
    // to store it on disk

    public UserDataManager(){
        users = new ConcurrentHashMap<>();
        usersInChat = new ArrayList<>();
    }

    public void join(String user){
        UserChatInformation ut = users.get(user);
        if (ut == null) {
            ut = new UserChatInformation();
            users.put(user, ut);
        }

        // Try to avoid duplicates, because newMessage() can also call join()
        if (!ut.isInChat()) {
            ut.joinChat();
            synchronized (lock) {
                usersInChat.add(user);
            }
        }
    }

    public void part(String user){
        UserChatInformation ut = users.get(user);
        if (ut != null){
            ut.leaveChat();
        }
        synchronized (lock) {
            usersInChat.remove(user);
        }
    }

    public void newMessage(String user){
        UserChatInformation chatInfo = users.get(user);
        if (chatInfo == null || !chatInfo.isInChat()){
            join(user);
            chatInfo = users.get(user);
        }

        chatInfo.updateLastMessageTime();
    }

    /*
        TODO: These standings methods are not ideal. Find some way to make them more efficient.
     */
    public List<String> topCurrencyStandings(int n){
        List<String> userList = getUsersAsList();
        Collections.sort(userList, new PointsComparator());

        return userList.stream().limit(n).collect(Collectors.toList());
    }

    public List<String> topViewTimeStandings(int n){
        List<String> userList = getUsersAsList();
        Collections.sort(userList, new TimeComparator());

        return userList.stream().limit(n).collect(Collectors.toList());
    }

    public int userCurrencyStanding(String user) throws NoSuchUserException {
        UserChatInformation chatInfo = users.get(user);
        if (chatInfo == null) throw new NoSuchUserException();

        List<String> userList = getUsersAsList();

        Collections.sort(userList, new PointsComparator());
        int pos = Collections.binarySearch(userList, user);

        return pos + 1; // Offset because 0 based index
    }

    public int userViewTimeStanding(String user) throws NoSuchUserException {
        UserChatInformation chatInfo = users.get(user);
        if (chatInfo == null) throw new NoSuchUserException();

        List<String> userList = getUsersAsList();

        Collections.sort(userList, new TimeComparator());
        int pos = Collections.binarySearch(userList, user);

        return pos + 1; // Offset because 0 based index
    }

    public int userCurrency(String user) throws NoSuchUserException {
        UserChatInformation chatInfo = users.get(user);
        if (chatInfo == null) throw new NoSuchUserException();

        return chatInfo.getAmountOfCurrency();
    }

    public long userTotalViewTime(String user) throws NoSuchUserException {
        UserChatInformation chatInfo = users.get(user);
        if (chatInfo == null) throw new NoSuchUserException();

        return chatInfo.getViewDuration();
    }

    public long userCreatedAt(String user) throws NoSuchUserException {
        UserChatInformation chatInfo = users.get(user);
        if (chatInfo == null) throw new NoSuchUserException();

        return chatInfo.getCreatedAt();
    }

    public List<String> activeUsers(int minutes){
        long currentTime = Instant.now().getEpochSecond();
        long seconds = minutes * 60;
        List<String> activeUsers = new ArrayList<>();
        synchronized (lock){
            return usersInChat.stream()
                    .filter(s -> currentTime - users.get(s).getLastMessageTime() < minutes)
                    .collect(Collectors.toList());
        }
    }

    private List<String> getUsersAsList() {
        List<String> list = new ArrayList<>(users.size());

        for(String s : users.keySet()){
            list.add(s);
        }

        return list;
    }

    class TimeComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            UserChatInformation u1 = users.get(o1);
            UserChatInformation u2 = users.get(o2);

            // Flip the order because the sort needs to be from biggest to smallest
            return (int) (u2.getViewDuration() - u1.getViewDuration());
        }
    }

    class PointsComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            UserChatInformation u1 = users.get(o1);
            UserChatInformation u2 = users.get(o2);

            // Flip because we want most currency first.
            return u2.getAmountOfCurrency() - u1.getAmountOfCurrency();
        }
    }

}
