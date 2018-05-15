package it.tdt.edu.vn.airmessenger.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class Message {
    public static final String COLLECTION_NAME = "messages";

    public static final String FIELD_SENDER_ID = "senderId";
    public static final String FIELD_SENDER_NAME = "senderName";
    public static final String FIELD_RECEIVER_ID = "receiverId";
    public static final String FIELD_RECEIVER_NAME = "receiverName";
    public static final String FIELD_TIME = "time";
    public static final String FIELD_CONTENT = "content";

    private String senderId;
    private String senderName;
    private String receiverId;
    private String receiverName;

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

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
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

    public static HashMap<String, Object> initMessageMap(
            String senderId,
            String senderName,
            String receiverId,
            String receiverName,
            String content) {

        HashMap<String, Object> message = new HashMap<>();

        message.put(FIELD_SENDER_ID, senderId);
        message.put(FIELD_SENDER_NAME, senderName);
        message.put(FIELD_RECEIVER_ID, receiverId);
        message.put(FIELD_RECEIVER_NAME, receiverName);
        message.put(FIELD_TIME, Calendar.getInstance().getTime());
        message.put(FIELD_CONTENT, content);

        return message;
    }
}
