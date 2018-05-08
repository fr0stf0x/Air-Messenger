package it.tdt.edu.vn.airmessenger.models;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

@IgnoreExtraProperties
public class Conversation {

    public static final String COLLECTION_NAME = "chats";

    public static final String FIELD_FIRST_USER = "firstUser";
    public static final String FIELD_SECOND_USER = "secondUser";
    public static final String FIELD_MESSAGE = "messageList";
    public static final String FIELD_PHOTO = "photo";
    public static final String FIELD_LAST_MSG_TIME = "lastMsgTime";
    public static final String FIELD_IS_READ = "isRead";

    private String firstUser;
    private String secondUser;
    private String chatPhoto;

    private @ServerTimestamp
    Date lastMsgTime;

    private List<Message> messageList;
    private boolean isRead;

    public Conversation() {
    }

    public Conversation(String firstUser, String secondUser) {
        this.firstUser = firstUser;
        this.secondUser = secondUser;
    }

    public String getChatPhoto() {
        return chatPhoto;
    }

    public void setChatPhoto(String chatPhoto) {
        this.chatPhoto = chatPhoto;
    }

    public void setLastMsgTime(Date lastMsgTime) {
        this.lastMsgTime = lastMsgTime;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getFirstUser() {
        return firstUser;
    }

    public void setFirstUser(String firstUser) {
        this.firstUser = firstUser;
    }

    public String getSecondUser() {
        return secondUser;
    }

    public void setSecondUser(String secondUser) {
        this.secondUser = secondUser;
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }
}
