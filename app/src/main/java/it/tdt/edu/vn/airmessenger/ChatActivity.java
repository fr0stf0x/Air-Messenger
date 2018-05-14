package it.tdt.edu.vn.airmessenger;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.tdt.edu.vn.airmessenger.adapters.MessageAdapter;
import it.tdt.edu.vn.airmessenger.interfaces.OnMessageClickListener;
import it.tdt.edu.vn.airmessenger.models.Conversation;
import it.tdt.edu.vn.airmessenger.models.Message;
import it.tdt.edu.vn.airmessenger.models.User;
import it.tdt.edu.vn.airmessenger.utils.FirebaseHelper;

/**
 * There are two ways to access this activity
 * 1. By clicking on a user from friend list
 * 2. By clicking on a conversation from conversation list
 */
public class ChatActivity extends AppCompatActivity implements OnMessageClickListener {

    final String TAG = "ChatActivity";

    @BindView(R.id.layout)
    ConstraintLayout layout;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.msgContent)
    EditText msgContent;

    @BindView(R.id.btnSend)
    ImageButton btnSend;

    @BindView(R.id.rvMessages)
    RecyclerView rvMessages;

    @BindView(R.id.tvUsername)
    TextView tvUserName;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.ivAvatar)
    ImageView ivAvatar;

    @BindView(R.id.tvOnlineStatus)
    TextView tvOnlineStatus;

    ActionBar actionBar;
    User receiveUser;
    FirebaseFirestore db;

    String chatId;
    String senderId;
    String receiverId;

    Query mQuery;
    MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ButterKnife.bind(this);
        initCore();
        getIntentData();
        setSupportActionBar(toolbar);
        updateUI();
    }

    private void initCore() {
        FirebaseUser firebaseSender = FirebaseHelper.getCurrentUser();
        db = FirebaseHelper.getFirestore();
        senderId = firebaseSender.getUid();
    }

    private void updateUI() {
        if (!receiverExists()) {
            actionBar.setTitle(getResources().getString(R.string.activity_chat_label));
        } else {
            setupActionBar();
            if (conversationExists()) {
                getMessages();
            }
        }
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendButtonClicked();
            }
        });
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            receiverId = intent.getStringExtra(User.USER_ID_KEY);
            chatId = intent.getStringExtra(Conversation.CONVERSATION_ID_KEY);
        }
    }

    private boolean conversationExists() {
        return chatId != null && !chatId.equals("");
    }

    private void getMessages() {
        mQuery = db.collection(Conversation.COLLECTION_NAME)
                .document(chatId).collection(Conversation.FIELD_MESSAGE);
        adapter = new MessageAdapter(mQuery, this);
        rvMessages.setAdapter(adapter);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        adapter.startListening();
    }


    public void onSendButtonClicked() {
        String message = getMessageContent();
        clearMessageInput();
        Log.d(TAG, "onSendButtonClicked: ChatId " + (chatId == null ? "null" : chatId));
        if (conversationExists()) {
            sendNewMessage(message);
        } else {
            initChat(message);
        }
    }

    private void sendNewMessage(String newMessage) {
        DocumentReference chatRef = db.collection(Conversation.COLLECTION_NAME)
                .document(chatId);
        DocumentReference newMessageRef = chatRef
                .collection(Conversation.FIELD_MESSAGE)
                .document();
        HashMap<String, Object> messageMap = Message.initMessage(senderId, receiverId, newMessage);
        WriteBatch batch = db.batch();
        batch.set(newMessageRef, messageMap);
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Conversation.FIELD_LAST_MSG_TIME, messageMap.get(Message.FIELD_TIME));
        batch.update(chatRef, updates);
        batch.commit()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Message sent");
                        } else {
                            Log.d(TAG, "onComplete: Message is not sent");
                        }
                    }
                });
    }

    private void initChat(String firstMessage) {
        WriteBatch batch = db.batch();
        DocumentReference chatRef = db.collection(Conversation.COLLECTION_NAME).document();
        chatId = chatRef.getId();

        DocumentReference messageRef = chatRef.collection(Conversation.FIELD_MESSAGE).document();

        HashMap<String, Object> newChat = Conversation.initConversation(senderId, receiverId);
        HashMap<String, Object> messageMap = Message.initMessage(senderId, receiverId, firstMessage);
        newChat.put(Conversation.FIELD_LAST_MSG_TIME, messageMap.get(Message.FIELD_TIME));

        batch.set(chatRef, newChat);
        batch.set(messageRef, messageMap);
        batch.set(createChatDocumentReference(chatId, senderId, receiverId), Conversation.initConversation(receiverId));
        batch.set(createChatDocumentReference(chatId, receiverId, senderId), Conversation.initConversation(senderId));

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Operation completed");
                }
            }
        });
    }

    private DocumentReference createChatDocumentReference(String documentId, String firstUserId, String secondUserId) {
        return db.collection(User.COLLECTION_NAME)
                .document(firstUserId).collection(Conversation.COLLECTION_NAME)
                .document(documentId);
    }

    private boolean receiverExists() {
        return receiverId != null && !receiverId.equals("");
    }

    private void setupActionBar() {
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(null);

        db.collection(User.COLLECTION_NAME).document(receiverId)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot receiveUserSnapshot = task.getResult();
                    receiveUser = receiveUserSnapshot.toObject(User.class);
                    if (receiveUser != null) {
                        tvUserName.setText(receiveUser.getName());
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    private String getMessageContent() {
        return msgContent.getText().toString();
    }

    private void clearMessageInput() {
        msgContent.setText("");
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        layout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMessageClicked(DocumentSnapshot msg) {

    }
}

