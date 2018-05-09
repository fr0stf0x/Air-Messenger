package it.tdt.edu.vn.airmessenger;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.tdt.edu.vn.airmessenger.models.FriendRequest;
import it.tdt.edu.vn.airmessenger.models.User;

public class UserInfoActivity extends AppCompatActivity {

    public static final String TAG = "UserInfo";


    @BindView(R.id.btn_add_friend)
    Button btnAddFriend;

    @BindView(R.id.tvUsername)
    TextView tvUsername;

    @BindView(R.id.tvStatus)
    TextView tvStatus;

    @BindView(R.id.tvAge)
    TextView tvAge;

    @BindView(R.id.tvSex)
    TextView tvSex;

    User refUser;
    FirebaseFirestore db;
    String refUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        db = FirebaseFirestore.getInstance();
        ButterKnife.bind(this);
        getUserInfo();
    }

    private void getUserInfo() {
        Intent intent = getIntent();
        if (intent != null) {
            refUserId = intent.getStringExtra(User.USER_ID_KEY);
            db.collection(User.COLLECTION_NAME)
                    .document(refUserId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                refUser = task.getResult().toObject(User.class);
                                if (refUser == null) {
                                    finish();
                                }
                                // TODO SET age and sex
                                tvAge.setText(refUser.getAge() + "");
                                tvSex.setText(refUser.getSex() + "");
                                tvStatus.setText(refUser.getStatus());
                                tvUsername.setText(refUser.getName());
                            }
                        }
                    });
        }
    }

    @OnClick(R.id.btn_add_friend)
    protected void addFriend(View view) {
        sendAddRequest();
    }

    private void sendAddRequest() {
        Log.d(TAG, "Send request triggered");
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        CollectionReference colRef = db.collection(User.COLLECTION_NAME)
                .document(firebaseUser.getUid()).collection(FriendRequest.COLLECTION_NAME);
        Date sendTime = Calendar.getInstance().getTime();
        HashMap<String, Object> request = new HashMap<>();
        request.put(FriendRequest.FIELD_USER_NAME, refUser.getName());
        request.put(FriendRequest.FIELD_SEND_TIME, sendTime);
        colRef.document(refUserId).set(request);
        btnAddFriend.setText("sent");
        btnAddFriend.setEnabled(false);
    }
}
