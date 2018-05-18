package it.tdt.edu.vn.airmessenger;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import it.tdt.edu.vn.airmessenger.adapters.MessageAdapter;
import it.tdt.edu.vn.airmessenger.interfaces.OnMessageClickListener;
import it.tdt.edu.vn.airmessenger.interfaces.RecyclerClickListener;
import it.tdt.edu.vn.airmessenger.models.Conversation;
import it.tdt.edu.vn.airmessenger.models.Message;
import it.tdt.edu.vn.airmessenger.models.User;
import it.tdt.edu.vn.airmessenger.utils.FirebaseHelper;

/**
 * There are two ways to access this activity
 * 1. By clicking on a user from friend list
 * 2. By clicking on a conversation from conversation list
 */
public class ChatActivity extends AppCompatActivity implements OnMessageClickListener, RecyclerClickListener {

    final String TAG = "ChatActivity";

    @BindView(R.id.layout)
    RelativeLayout layout;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.edtMessage)
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
    String senderPhoto;
    String receiverPhoto;
    String senderId;
    String senderName;
    String receiverId;
    String receiverName;

    Query mQuery;
    MessageAdapter adapter;
    private ActionMode mActionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getIntentData();
        initCore();
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

            @Override
            protected void onDataChanged() {
                if (adapter.getItemCount() == 0) {
                    Conversation.deleteConversation(chatId, senderId, receiverId);
                }
            }

            @Override
            protected void onDocumentRemoved(final DocumentChange change) {
                super.onDocumentRemoved(change);

                Log.d(TAG, "onComplete: oldIndex = " + change.getOldIndex());

                if (change.getOldIndex() == 0) {
                    Log.d(TAG, "onComplete: need to delete conversation");
                    return;
                }
                db.collection(Conversation.COLLECTION_NAME)
                        .document(chatId)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    Map<String, Object> msgToDeleteMap =
                                            (Map<String, Object>) task
                                                    .getResult()
                                                    .get(Conversation.FIELD_LAST_MESSAGE);

                                    String msgToDeleteId = (String) msgToDeleteMap
                                            .get(Message.FIELD_MESSAGE_ID);
                                    if (msgToDeleteId.equals(change.getDocument().getId())) {

                                        Message newLastMessage = adapter
                                                .getSnapshot((change.getOldIndex() - 1))
                                                .toObject(Message.class);

                                        if (newLastMessage != null) {
                                            Map<String, Object> newLastMsgMap = Message
                                                    .initMessageMap(newLastMessage);

                                            Map<String, Object> update = new HashMap<>();
                                            update.put(Conversation.FIELD_LAST_MESSAGE, newLastMsgMap);
                                        }
                                    }
                                }
                            }
                        });
            }
        };
        rvMessages.setAdapter(adapter);
//        setAdapterOnClick();
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        adapter.startListening();
    }

    @Override
    public void onItemSelected(int position) {
        adapter.toggleSelection(position);//Toggle the selection
        Toast.makeText(this, "Selected " + position, Toast.LENGTH_SHORT).show();
        boolean hasCheckedItems = adapter.getSelectedCount() > 0;//Check if any items are already selected or not

        if (hasCheckedItems && mActionMode == null)
            // there are some selected items, start the actionMode
            mActionMode = startSupportActionMode(new MessageActionModeCallBack(adapter) {
                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    adapter.removeSelection();
                    Log.d(TAG, "onDestroyActionMode: setNullToActionMode");
                    setNullToActionMode();
                }
            });
        else if (!hasCheckedItems && mActionMode != null)
            // there no selected items, finish the actionMode
            mActionMode.finish();
        if (mActionMode != null)
            //set action mode title on item selection
            mActionMode.setTitle(String.valueOf(adapter
                    .getSelectedCount()));
    }


    //Set action mode null after use
    public void setNullToActionMode() {
        if (mActionMode != null) {
            Log.d(TAG, "setNullToActionMode: Completed");
            mActionMode = null;
        }
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
        String newMessageId = newMessageRef.getId();

        Map<String, Object> messageMap = Message.initMessageMap(
                newMessageId,
                senderId,
                senderName,
                receiverId,
                receiverName,
                messageContent);

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
        String newMessageId = messageRef.getId();
        chatId = centralConversationRef.getId();
        Map<String, Object> messageMap = Message.initMessageMap(
                newMessageId,
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
    public void onMessageClicked(int position) {
        Log.d(TAG, "onMessageClicked: At " + position);
        if (mActionMode != null) {
            onItemSelected(position);
        }
    }

    @Override
    public boolean onMessageLongClicked(int position) {
        Log.d(TAG, "onMessageLongClicked: At " + position);
        onItemSelected(position);
        return true;
    }


    class MessageActionModeCallBack implements ActionMode.Callback, DialogInterface.OnClickListener {
        final String TAG = "ActionModeCallBack";

        private MessageAdapter adapter;
        SparseBooleanArray selected;
        int selectedMessageSize;

        public MessageActionModeCallBack(MessageAdapter adapter) {
            this.adapter = adapter;
            if (adapter != null) {
                adapter.startListening();
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.message_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            selected = adapter.getSelectedIds();
            selectedMessageSize = selected.size();

            switch (item.getItemId()) {
                case R.id.action_copy:
                    ClipboardManager clipboard = (ClipboardManager) App.getContext()
                            .getSystemService(CLIPBOARD_SERVICE);

                    StringBuffer builder = new StringBuffer();
                    for (int i = (selectedMessageSize - 1); i >= 0; i--) {
                        if (selected.valueAt(i)) {
                            DocumentSnapshot message = adapter.getSnapshot(i);
                            builder.append(message.getString(Message.FIELD_SENDER_NAME)
                                    + ": "
                                    + message.getString(Message.FIELD_CONTENT)
                                    + "\n");
                        }
                    }
                    ClipData clip = ClipData.newPlainText("Copy message", builder);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(App.getContext(),
                            App.getContext().getString(R.string.copy_message_success),
                            Toast.LENGTH_SHORT)
                            .show();
                    mode.finish();
                    break;

                case R.id.action_delete:
                    String arg, alertTitle;
                    if (selectedMessageSize == adapter.getSelectedCount()) {
                        arg = getString(R.string.arg_conversation);
                    } else {
                        if (selectedMessageSize > 1) {
                            arg = getString(R.string.arg_messages);
                        } else {
                            arg = getString(R.string.arg_message);
                        }
                    }
                    alertTitle = String.format(Locale.getDefault(),
                            getString(R.string.delete_alert), arg);
                    createDialog(alertTitle).show();
                    mode.finish();
                    break;
                case R.id.action_forward:
                    Toast.makeText(App.getContext(), "Function 'forward' is WIP",
                            Toast.LENGTH_SHORT)
                            .show();
                case R.id.action_thumb_up:
                    Toast.makeText(App.getContext(), "Function 'thumb' is WIP",
                            Toast.LENGTH_SHORT)
                            .show();
                case R.id.action_thumb_down:
                    Toast.makeText(App.getContext(), "Function 'thumb' is WIP",
                            Toast.LENGTH_SHORT)
                            .show();
            }
            return true;
        }

        private void deleteMessages() {
            WriteBatch batch = FirebaseHelper.getFirestore().batch();
            for (int i = (selected.size() - 1); i >= 0; i--) {
                if (selected.valueAt(i)) {
                    //get selected data in Model
                    Log.d(TAG, "onActionItemClicked: index " + i);
                    DocumentSnapshot message = adapter.getSnapshot(i);
                    batch.delete(message.getReference());
                }
            }
            batch.commit()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(App.getContext(),
                                        String.format(Locale.getDefault(),
                                                App.getContext().getString(R.string.delete_success), " message(s)"),
                                        Toast.LENGTH_SHORT)
                                        .show();
                            } else {
                                Toast.makeText(App.getContext(),
                                        String.format(Locale.getDefault(),
                                                App.getContext().getString(R.string.delete_fail), " message(s)"),
                                        Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    });
        }

        private AlertDialog createDialog(String alertString) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
            AlertDialog dialog;
            dialog = builder
                    .setMessage(alertString)
                    .setPositiveButton(
                            R.string.delete_okay, this)
                    .setNegativeButton(
                            R.string.cancel, this)
                    .create();
            return dialog;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_NEGATIVE) {
                dialog.dismiss();
            } else if (which == DialogInterface.BUTTON_POSITIVE) {
                deleteMessages();
                dialog.dismiss();
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    }
}

