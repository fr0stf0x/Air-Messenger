package it.tdt.edu.vn.airmessenger.models;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

@IgnoreExtraProperties
public class FriendRequest {
    public static final String COLLECTION_NAME = "friendRequests";

    public static final String FIELD_SEND_TIME = "sendTime";
    public static final String FIELD_USER_NAME = "userName";

    private @ServerTimestamp
    Date sendTime;
    private String userName;

    public FriendRequest() {
    }

    public FriendRequest(String userName, Date sendTime) {
        this.userName = userName;
        this.sendTime = sendTime;
    }

    public String getUserName() {
        return userName;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date date) {
        this.sendTime = date;
    }
}
