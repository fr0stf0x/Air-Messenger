package it.tdt.edu.vn.airmessenger.models;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@IgnoreExtraProperties
public class Conversation {

    public static final String COLLECTION_NAME = "chats";
    public static final String CONVERSATION_ID_KEY = "conversationId";

    public static final String FIELD_FIRST_USER = "firstUserId";
    public static final String FIELD_SECOND_USER = "secondUserId";
    public static final String FIELD_MESSAGE = "messages";
    public static final String FIELD_PHOTO = "chatPhoto";
    public static final String FIELD_LAST_MSG_TIME = "lastMsgTime";
    public static final String FIELD_WITH = "with";

    private String firstUserId;
    private String secondUserId;
    private String chatPhoto;
    private @ServerTimestamp
    Date lastMsgTime;
    private List<Message> messages;

    public Conversation() {
    }

    public Conversation(String firstUser, String secondUser) {
        this.firstUserId = firstUser;
        this.secondUserId = secondUser;
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

    public String getFirstUserId() {
        return firstUserId;
    }

    public void setFirstUserId(String firstUserId) {
        this.firstUserId = firstUserId;
    }

    public String getSecondUserId() {
        return secondUserId;
    }

    public void setSecondUserId(String secondUserId) {
        this.secondUserId = secondUserId;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public static HashMap<String, Object> initConversation(String firstUserId, String secondUserId) {
        HashMap<String, Object> result = new HashMap<>();
        result.put(FIELD_FIRST_USER, firstUserId);
        result.put(FIELD_SECOND_USER, secondUserId);
        return result;
    }

    public static HashMap<String, Object> initConversation(String secondUserId) {
        HashMap<String, Object> result = new HashMap<>();
        result.put(FIELD_WITH, secondUserId);
        return result;
    }
}
