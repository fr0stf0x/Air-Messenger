package it.tdt.edu.vn.airmessenger.models;

import android.support.annotation.NonNull;

public class Message implements Comparable<Message> {
    public static final String COLLECTION_NAME = "messages";

    public static final String FIELD_SEND_USER = "send_user";
    public static final String FIELD_RECEIVE_USER = "receive_user";
    public static final String FIELD_TIME = "time";
    public static final String FIELD_CONTENT = "content";

    private String sendUser;
    private String receiveUser;
    private String time;
    private String content;

    public Message(String sendUser, String receiveUser, String time, String content) {
        this.sendUser = sendUser;
        this.receiveUser = receiveUser;
        this.time = time;
        this.content = content;
    }

    public String getSendUser() {
        return sendUser;
    }

    public String getReceiveUser() {
        return receiveUser;
    }


    public String getTime() {
        return time;
    }


    public String getContent() {
        return content;
    }

    @Override
    public int compareTo(@NonNull Message message) {
        return 0;
    }
}
