package it.tdt.edu.vn.airmessenger;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
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

import it.tdt.edu.vn.airmessenger.loaders.Loader;
import it.tdt.edu.vn.airmessenger.loaders.UserLoader;
import it.tdt.edu.vn.airmessenger.models.User;

public class ChatActivity extends AppCompatActivity {

    final String TAG = "ChatActivity";

    EditText msgContent;
    ImageButton btnSend;
    RecyclerView rvMessages;
    ActionBar actionBar;

    UserLoader loader;
    FirebaseFirestore db;
    CollectionReference colRef;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        actionBar = getSupportActionBar();
        try {
            actionBar.setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            Log.d("ActionBarSetting", e.getMessage());
        }

        db = FirebaseFirestore.getInstance();
        loader = (UserLoader) Loader.getUserLoader();
        colRef = db.collection("users");

        msgContent = findViewById(R.id.msgContent);
        rvMessages = findViewById(R.id.rvMessages);
        btnSend = findViewById(R.id.btnSend);
        setTitle(actionBar);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null) {
                    Toast.makeText(getApplicationContext(), user.getDisplayName() + "", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ChatActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setTitle(final ActionBar actionBar) {
        Intent intent = getIntent();
        if (intent != null) {
            String userId = intent.getStringExtra(User.FIELD_ID);
            user = new User(userId);
            DocumentReference document = colRef.document(userId);
            loader.load(document).into(user);
            document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        actionBar.setTitle(task.getResult().getString(User.FIELD_NAME));
                    }
                }
            });
        } else {
            actionBar.setTitle(getResources().getString(R.string.activity_chat_label));
        }
    }
}

