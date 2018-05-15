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
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import it.tdt.edu.vn.airmessenger.AllUsersActivity;
import it.tdt.edu.vn.airmessenger.ChatActivity;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.UserInfoActivity;
import it.tdt.edu.vn.airmessenger.adapters.UserAdapter;
import it.tdt.edu.vn.airmessenger.interfaces.OnUserClickListener;
import it.tdt.edu.vn.airmessenger.models.Conversation;
import it.tdt.edu.vn.airmessenger.models.User;

public class UserListFragment extends Fragment implements OnUserClickListener {

    public static String COLLECTION_NAME = "friends";

    public static final String TAG = "User_list_fragment";

    public static final int USERS_FLAG = 1;
    public static final int FRIENDS_FLAG = 0;
    private int flag = FRIENDS_FLAG;

    public static final int LIMIT = 50;

    FirebaseUser user;
    FirebaseFirestore db;

    RecyclerView rvContacts;

    TextView tvEmptyList;

    UserAdapter adapter;
    Query mQuery;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if (context instanceof AllUsersActivity) {
            flag = USERS_FLAG;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    public void onUserClick(DocumentSnapshot user) {
        String userId = user.getId();
        Intent intent;
        switch (flag) {
            case USERS_FLAG:
                intent = new Intent(getContext(), UserInfoActivity.class);
                startIntentWithExtra(intent, userId);
                break;
            default: // jump to chat activity
                intent = new Intent(getContext(), ChatActivity.class);
                putExtrasAndStart(intent, userId);
                break;
        }
    }

    private void startIntentWithExtra(Intent intent, String userId) {
        intent.putExtra(User.USER_ID_KEY, userId);
        startActivity(intent);
    }

    private void putExtrasAndStart(final Intent intent, final String receiverId) {
        db.collection(User.COLLECTION_NAME)
                .document(this.user.getUid())
                .collection(Conversation.COLLECTION_NAME)
                .whereEqualTo(User.FIELD_CHAT_WITH, receiverId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> result = task.getResult().getDocuments();
                            if (result.size() > 1) {
                                Log.d(TAG, "onComplete: ERROR - More than 1 conversation");
                                return;
                            }
                            if (result.size() == 1) {
                                String chatId = result.get(0).getId();
                                intent.putExtra(Conversation.CONVERSATION_ID_KEY, chatId);
                                Log.d(TAG, "onComplete: There 1 conversation: " + chatId);
                            }
                            startIntentWithExtra(intent, receiverId);
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.contact_list_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvEmptyList = view.findViewById(R.id.tvEmptyList);
        rvContacts = view.findViewById(R.id.userList);

        if (user != null) {
            switch (flag) {
                case FRIENDS_FLAG:
                    mQuery = db.collection(User.COLLECTION_NAME)
                            .document(user.getUid()).collection(COLLECTION_NAME);
                    Log.d(TAG, "Case friends");
                    break;
                case USERS_FLAG:
                    mQuery = db.collection(User.COLLECTION_NAME);
                    Log.d(TAG, "Case users");
                    break;
            }

            adapter = new UserAdapter(mQuery, this, flag) {
                @Override
                protected void onDataChanged() {
                    if (flag == FRIENDS_FLAG) {
                        if (getItemCount() == 0) {
                            rvContacts.setVisibility(View.GONE);
                            tvEmptyList.setVisibility(View.VISIBLE);
                        } else {
                            rvContacts.setVisibility(View.VISIBLE);
                            tvEmptyList.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                protected void onError(FirebaseFirestoreException e) {
                    Log.d(TAG, "Error: check logs for info.");
                    rvContacts.setVisibility(View.GONE);
                    tvEmptyList.setVisibility(View.GONE);
                }
            };

            rvContacts.setLayoutManager(new LinearLayoutManager(getContext()));
            rvContacts.setAdapter(adapter);
        }
    }

}
