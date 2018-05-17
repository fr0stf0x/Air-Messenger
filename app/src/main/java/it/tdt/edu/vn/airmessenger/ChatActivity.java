package it.tdt.edu.vn.airmessenger;

import android.content.Intent;
import android.support.annotation.NonNull;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
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
    RelativeLayout layout;

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
    CircleImageView ivAvatar;

    @BindView(R.id.tvOnlineStatus)
    TextView tvOnlineStatus;

    ActionBar actionBar;
    User senderUser;
    User receiveUser;
    FirebaseFirestore db;
    FirebaseUser firebaseSender;

    String chatId;
    String senderPhoto = "";
    String receiverPhoto = "";
    String senderId;
    String senderName;
    String receiverId;
    String receiverName;

    Query mQuery;
    MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getIntentData();
        initCore();
    }

    private boolean dataLegal() {
        return receiverExists() && conversationExists();
    }

    private void initCore() {
        db = FirebaseHelper.getFirestore();
        firebaseSender = FirebaseHelper.getCurrentUser();
        senderId = firebaseSender.getUid();
        senderName = firebaseSender.getDisplayName();
        db.collection(User.COLLECTION_NAME)
                .document(senderId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            senderUser = task.getResult().toObject(User.class);
                            if (senderUser != null) {
                                if (senderUser.getThumbImage() != null) {
                                    senderPhoto = senderUser.getThumbImage();
                                }
                            }
                            setupUI();
                        }
                    }
                });
    }

    private void setupUI() {
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(null);

        if (receiverExists()) {
            updateUI();
        }

        Log.d(TAG, "setupUI: SenderPhoto " + senderPhoto);
        Log.d(TAG, "setupUI: ReceiverPhoto " + receiverPhoto);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendButtonClicked();
            }
        });
    }

    private void updateUI() {
        db.collection(User.COLLECTION_NAME).document(receiverId)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot receiveUserSnapshot = task.getResult();
                    receiveUser = receiveUserSnapshot.toObject(User.class);
                    if (receiveUser != null) {
                        if (receiveUser.getThumbImage() != null) {
                            receiverPhoto = receiveUser.getThumbImage();
                            putImage(receiverPhoto, ivAvatar);
                        }
                        tvUserName.setText(receiverName);
                        receiverName = receiveUser.getName();
                        if (conversationExists()) {
                            getMessages();
                        }
                    }
                }
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

    private void putImage(String photoUrl, CircleImageView ivAvatar) {
        Picasso.get()
                .load(photoUrl)
                .into(ivAvatar);
    }

    private void getMessages() {
        mQuery = db.collection(Conversation.COLLECTION_NAME)
                .document(chatId).collection(Conversation.FIELD_MESSAGES)
                .orderBy(Message.FIELD_TIME, Query.Direction.ASCENDING);

        adapter = new MessageAdapter(mQuery, senderPhoto, receiverPhoto, this) {
            @Override
            protected void onDocumentAdded(DocumentChange change) {
                super.onDocumentAdded(change);
                rvMessages.smoothScrollToPosition(change.getNewIndex());
            }
        };
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
            initConversation(message);
        }
    }

    private void sendNewMessage(String messageContent) {

        DocumentReference senderChatRef = db.collection(User.COLLECTION_NAME)
                .document(senderId)
                .collection(Conversation.COLLECTION_NAME)
                .document(chatId);
        DocumentReference receiverChatRef = db.collection(User.COLLECTION_NAME)
                .document(receiverId)
                .collection(Conversation.COLLECTION_NAME)
                .document(chatId);
        DocumentReference newMessageRef = db.collection(Conversation.COLLECTION_NAME)
                .document(chatId)
                .collection(Conversation.FIELD_MESSAGES)
                .document();

        Map<String, Object> messageMap = Message.initMessageMap(senderId, senderName,
                receiverId, receiverName, messageContent);

        Map<String, Object> lastMessageUpdate = new HashMap<>();
        lastMessageUpdate.put(Conversation.FIELD_LAST_MESSAGE, messageMap);

        db.batch()
                .set(newMessageRef, messageMap)
                .update(senderChatRef, lastMessageUpdate)
                .update(receiverChatRef, lastMessageUpdate)
                .commit()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: New message sent");
                        } else {
                            Log.d(TAG, "onComplete: New message is not sent");
                        }
                    }
                });
    }

    private void initConversation(String firstMessage) {
        DocumentReference centralConversationRef = db.collection(Conversation.COLLECTION_NAME).document();

        DocumentReference messageRef = centralConversationRef.collection(Conversation.FIELD_MESSAGES).document();
        chatId = centralConversationRef.getId();
        Map<String, Object> messageMap = Message.initMessageMap(
                senderId,
                senderName,
                receiverId,
                receiverName,
                firstMessage);

        Map<String, Object> centralConversationMap = Conversation.initCentralConversation(
                senderId, receiverId, messageMap);

        DocumentReference senderConversationRef = createChatDocumentReference(chatId, senderId);
        Map<String, Object> senderConversationMap = Conversation.initUserConversation(receiverName, receiverId,
                receiverPhoto, messageMap);

        DocumentReference receiverConversationRef = createChatDocumentReference(chatId, receiverId);
        Map<String, Object> receiverConversationMap = Conversation.initUserConversation(senderName, senderId,
                senderPhoto, messageMap);

        db.batch()
                .set(centralConversationRef, centralConversationMap)
                .set(messageRef, messageMap)
                .set(senderConversationRef, senderConversationMap)
                .set(receiverConversationRef, receiverConversationMap)
                .commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Initialization completed");
                    updateUI();
                } else {
                    Log.d(TAG, "onComplete: Initialization failed");
                }
            }
        });
    }

    private DocumentReference createChatDocumentReference(String documentId, String refUser) {
        return db.collection(User.COLLECTION_NAME)
                .document(refUser).collection(Conversation.COLLECTION_NAME)
                .document(documentId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: SetupUI");
        setupUI();
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

    private boolean receiverExists() {
        return receiverId != null && !receiverId.equals("");
    }

    private boolean conversationExists() {
        return chatId != null && !chatId.equals("");
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

