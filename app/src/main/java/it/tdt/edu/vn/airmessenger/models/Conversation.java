package it.tdt.edu.vn.airmessenger.models;

import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import it.tdt.edu.vn.airmessenger.App;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.utils.FirebaseHelper;

@IgnoreExtraProperties
public class Conversation {

    public static final String COLLECTION_NAME = "chats";
    public static final String CONVERSATION_ID_KEY = "conversationId";

    public static final String FIELD_FIRST_USER = "firstUserId";
    public static final String FIELD_SECOND_USER = "secondUserId";
    public static final String FIELD_CHAT_PHOTO = "chatPhoto";
    public static final String FIELD_MESSAGES = "messages";
    public static final String FIELD_LAST_MESSAGE = "lastMessage";
    public static final String FIELD_CONVERSATION_NAME = "conversationName";


    private String firstUserId;
    private String secondUserId;
    private String chatPhoto;
    private Map<String, Object> lastMessage;
    private String conversationName;

    public Conversation() {

    }

    public String getChatPhoto() {
        return chatPhoto;
    }

    public void setChatPhoto(String chatPhoto) {
        this.chatPhoto = chatPhoto;
    }

    public String getFirstUserId() {
        return firstUserId;
    }

    public void setFirstUserId(String firstUserId) {
        this.firstUserId = firstUserId;
    }

    public String getSecondUserId() {
        return secondUserId;
    }

    public void setSecondUserId(String secondUserId) {
        this.secondUserId = secondUserId;
    }

    public Map<String, Object> getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Map<String, Object> lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getConversationName() {
        return conversationName;
    }

    public void setConversationName(String conversationName) {
        this.conversationName = conversationName;
    }

    public static void deleteConversation(String chatId, String senderId, String receiverId) {
        FirebaseFirestore db = FirebaseHelper.getFirestore();
        final DocumentReference centralRef = db
                .collection(Conversation.COLLECTION_NAME)
                .document(chatId);

        final DocumentReference senderRef = db
                .collection(User.COLLECTION_NAME)
                .document(senderId)
                .collection(Conversation.COLLECTION_NAME)
                .document(chatId);
        final DocumentReference receiverRef = db
                .collection(User.COLLECTION_NAME)
                .document(receiverId)
                .collection(Conversation.COLLECTION_NAME)
                .document(chatId);
        Query query = centralRef.collection(Conversation.FIELD_MESSAGES);
        final WriteBatch batch = db.batch();
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot message : task.getResult().getDocuments()) {
                        DocumentReference messageToDel = message.getReference();
                        batch.delete(messageToDel);
                    }
                    batch
                            .delete(centralRef)
                            .delete(senderRef)
                            .delete(receiverRef)
                            .commit()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(App.getContext(),
                                            String.format(Locale.getDefault(), App.getContext().getResources()
                                                    .getString(R.string.delete_success), "conversation"),
                                            Toast.LENGTH_SHORT)
                                            .show();
                                }
                            });
                }
            }
        });

    }

    public static Map<String, Object> initCentralConversation(String firstUserId,
                                                              String secondUserId,
                                                              Map<String, Object> lastMessageMap) {
        Map<String, Object> result = new HashMap<>();
        result.put(FIELD_FIRST_USER, firstUserId);
        result.put(FIELD_SECOND_USER, secondUserId);
        result.put(FIELD_LAST_MESSAGE, lastMessageMap);
        return result;
    }

    public static Map<String, Object> initUserConversation(String conversationName,
                                                           String secondUserId,
                                                           String chatPhoto,
                                                           Map<String, Object> lastMessageMap) {
        Map<String, Object> result = new HashMap<>();
        result.put(FIELD_CONVERSATION_NAME, conversationName);
        result.put(User.FIELD_CHAT_WITH, secondUserId);
        result.put(FIELD_CHAT_PHOTO, chatPhoto);
        result.put(FIELD_LAST_MESSAGE, lastMessageMap);
        return result;
    }
}
