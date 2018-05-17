package it.tdt.edu.vn.airmessenger.models;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Conversation {

    public static final String COLLECTION_NAME = "chats";
    public static final String CONVERSATION_ID_KEY = "conversationId";

    public static final String FIELD_FIRST_USER = "firstUserId";
    public static final String FIELD_SECOND_USER = "secondUserId";
    public static final String FIELD_CHAT_PHOTO = "chatPhoto";
    public static final String FIELD_MESSAGES = "messages";
    public static final String FIELD_LAST_MESSAGE = "lastMessage";
    public static final String FIELD_CONVERSATION_NAME = "conversationName";


    private String firstUserId;
    private String secondUserId;
    private String chatPhoto;
    private Map<String, Object> lastMessage;
    private String conversationName;

    public Conversation() {

    }

    public String getChatPhoto() {
        return chatPhoto;
    }

    public void setChatPhoto(String chatPhoto) {
        this.chatPhoto = chatPhoto;
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

    public Map<String, Object> getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Map<String, Object> lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getConversationName() {
        return conversationName;
    }

    public void setConversationName(String conversationName) {
        this.conversationName = conversationName;
    }

    public static Map<String, Object> initCentralConversation(String firstUserId,
                                                              String secondUserId,
                                                              Map<String, Object> lastMessageMap) {
        Map<String, Object> result = new HashMap<>();
        result.put(FIELD_FIRST_USER, firstUserId);
        result.put(FIELD_SECOND_USER, secondUserId);
        result.put(FIELD_LAST_MESSAGE, lastMessageMap);
        return result;
    }

    public static Map<String, Object> initUserConversation(String conversationName,
                                                           String secondUserId,
                                                           String chatPhoto,
                                                           Map<String, Object> lastMessageMap) {
        Map<String, Object> result = new HashMap<>();
        result.put(FIELD_CONVERSATION_NAME, conversationName);
        result.put(User.FIELD_CHAT_WITH, secondUserId);
        result.put(FIELD_CHAT_PHOTO, chatPhoto);
        result.put(FIELD_LAST_MESSAGE, lastMessageMap);
        return result;
    }
}
