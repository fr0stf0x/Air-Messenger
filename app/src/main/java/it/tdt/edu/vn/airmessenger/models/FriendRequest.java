package it.tdt.edu.vn.airmessenger.models;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.WriteBatch;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import it.tdt.edu.vn.airmessenger.App;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.UserInfoActivity;
import it.tdt.edu.vn.airmessenger.utils.FirebaseHelper;

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

    public static Task<Void> sendRequest(String senderId, String senderName, String receiverId) {
        // Get receiver reference to get the name

        Date sendTime = Calendar.getInstance().getTime();
        HashMap<String, Object> request = new HashMap<>();

        request.put(FriendRequest.FIELD_USER_NAME,
                senderName);
        request.put(FriendRequest.FIELD_SEND_TIME, sendTime);

        return FirebaseHelper.getFirestore().collection(User.COLLECTION_NAME)
                .document(receiverId)
                .collection(FriendRequest.COLLECTION_NAME)
                .document(senderId)
                .set(request);
    }

    public static Task<Void> undoSendRequest(String senderId, String receiverId) {
        FirebaseFirestore db = FirebaseHelper.getFirestore();
        WriteBatch batch = db.batch();
        return batch.delete(db.collection(User.COLLECTION_NAME).document(receiverId).collection(FriendRequest.COLLECTION_NAME).document(senderId))
                .commit();
    }
}
