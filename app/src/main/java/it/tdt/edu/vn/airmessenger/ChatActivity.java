package it.tdt.edu.vn.airmessenger;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.tdt.edu.vn.airmessenger.fragments.UserListFragment;
import it.tdt.edu.vn.airmessenger.loaders.Loader;
import it.tdt.edu.vn.airmessenger.loaders.UserLoader;
import it.tdt.edu.vn.airmessenger.models.User;

public class ChatActivity extends AppCompatActivity {

    final String TAG = "ChatActivity";

    @BindView(R.id.msgContent)
    EditText msgContent;

    @BindView(R.id.btnSend)
    ImageButton btnSend;

    @BindView(R.id.rvMessages)
    RecyclerView rvMessages;

    ActionBar actionBar;

    FirebaseFirestore db;
    User receiveUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        setupActionBar();
    }

    @OnClick(R.id.btnSend)
    public void onSendButtonClicked() {
        if (receiveUser != null) {
            Toast.makeText(getApplicationContext(), receiveUser.getName() + "", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ChatActivity.this, "Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupActionBar() {
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(null);

        Intent intent = getIntent();
        if (intent != null) {
            String receiverId = intent.getStringExtra(User.USER_ID_KEY);

            db.collection(User.COLLECTION_NAME).document(receiverId)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot receiveUserSnapshot = task.getResult();
//                        actionBar.setTitle(receiveUserSnapshot.getString(User.FIELD_NAME));
                        receiveUser = receiveUserSnapshot.toObject(User.class);
                        Log.d(TAG, "Get receiver info successfully " + receiveUserSnapshot.getString(User.FIELD_NAME));
                    }
                }
            });
        } else {
            Log.d(TAG, "No extra, set default label");
            actionBar.setTitle(getResources().getString(R.string.activity_chat_label));
        }
    }
}

