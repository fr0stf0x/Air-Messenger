package it.tdt.edu.vn.airmessenger.models;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Message {
    public static final String COLLECTION_NAME = "messages";

    public static final String FIELD_SEND_USER = "send_user";
    public static final String FIELD_RECEIVE_USER = "receive_user";
    public static final String FIELD_TIME = "time";
    public static final String FIELD_CONTENT = "content";

    private String sendUserId;
    private String receiveUserId;
    private @ServerTimestamp
    Date time;
    private String content;

    public Message(User sendUser, User receiveUser, String content) {
//        this.sendUserId = sendUser.getUserId();
//        this.receiveUserId = receiveUser.getUserId();
        this.content = content;
    }

    public String getSendUserId() {
        return sendUserId;
    }

    public String getReceiveUserId() {
        return receiveUserId;
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

}
