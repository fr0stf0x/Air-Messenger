package it.tdt.edu.vn.airmessenger.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.tdt.edu.vn.airmessenger.App;
import it.tdt.edu.vn.airmessenger.ChatActivity;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.adapters.ConversationAdapter;
import it.tdt.edu.vn.airmessenger.interfaces.OnChatSelectedListener;
import it.tdt.edu.vn.airmessenger.models.Conversation;
import it.tdt.edu.vn.airmessenger.models.Message;
import it.tdt.edu.vn.airmessenger.models.User;

public class ConversationListFragment extends Fragment implements OnChatSelectedListener {
    FirebaseUser firebaseUser;
    ConversationAdapter adapter;
    Query mQuery;
    FirebaseFirestore db;

    final String TAG = "ConversationList";

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.list_conversation)
    RecyclerView rvChats;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            Log.d(TAG, "onStop: Adapter started listening");
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            Log.d(TAG, "onStop: Adapter stopped listening");
            adapter.stopListening();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.conversation_list_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        progressBar.setVisibility(View.GONE);
        mQuery = db.collection(User.COLLECTION_NAME)
                .document(firebaseUser.getUid())
                .collection(Conversation.COLLECTION_NAME)
                .orderBy(Conversation.FIELD_LAST_MESSAGE + "." + Message.FIELD_TIME, Query.Direction.ASCENDING);
        adapter = new ConversationAdapter(mQuery, this);
        rvChats.setAdapter(adapter);
        rvChats.setLayoutManager(new LinearLayoutManager(App.getContext()));
    }

    @Override
    public void onConversationClicked(DocumentSnapshot chat) {
        String chatId = chat.getId();
        String receiverId = chat.getString(User.FIELD_CHAT_WITH);
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(User.USER_ID_KEY, receiverId);
        intent.putExtra(Conversation.CONVERSATION_ID_KEY, chatId);
        startActivity(intent);
    }
}
