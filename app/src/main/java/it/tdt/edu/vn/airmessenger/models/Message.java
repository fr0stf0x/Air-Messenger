package it.tdt.edu.vn.airmessenger.models;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class Message {
    public static final String COLLECTION_NAME = "messages";

    public static final String FIELD_SEND_USER = "senderId";
    public static final String FIELD_RECEIVE_USER = "receiverId";
    public static final String FIELD_TIME = "time";
    public static final String FIELD_CONTENT = "content";

    private String senderId;
    private String receiverId;

    private @ServerTimestamp
    Date time;
    private String content;

    public Message() {
    }

    public Message(String senderId, String receiverId, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public Date getTime() {
        return time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public static HashMap<String, Object> initMessage(String senderId, String receiverId, String content) {
        HashMap<String, Object> message = new HashMap<>();
        message.put(FIELD_SEND_USER, senderId);
        message.put(FIELD_RECEIVE_USER, receiverId);
        message.put(FIELD_CONTENT, content);
        message.put(FIELD_TIME, Calendar.getInstance().getTime());
        return message;
    }
}
