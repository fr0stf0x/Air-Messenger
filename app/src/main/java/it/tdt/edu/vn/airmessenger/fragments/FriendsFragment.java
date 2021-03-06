package it.tdt.edu.vn.airmessenger.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.adapters.FriendRequestAdapter;
import it.tdt.edu.vn.airmessenger.models.FriendRequest;
import it.tdt.edu.vn.airmessenger.models.User;

public class FriendsFragment extends Fragment implements FriendRequestAdapter.FriendRequestHandler {

    public static final String REQUEST_TAG = "FriendRequest";
    public static final String FRIEND_TAG = "FriendList";

    @BindView(R.id.frameAllFriends)
    FrameLayout frameAllFriends;

    @BindView(R.id.tvNotification)
    TextView tvNotification;

    @BindView(R.id.requestList)
    RecyclerView requestList;

    FirebaseUser user;
    FirebaseFirestore db;

    FragmentManager fm;
    FragmentTransaction ft;

    Query mQuery;

    FriendRequestAdapter adapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }


    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
            Log.d(REQUEST_TAG, "FriendRequestAdapter started listening");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
            Log.d(REQUEST_TAG, "FriendRequestAdapter stopped listening");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.friend_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (user != null) {
            ButterKnife.bind(this, view);

            fm = getChildFragmentManager();
            ft = fm.beginTransaction();

            ft.replace(R.id.frameAllFriends,
                    new UserListFragment(),
                    UserListFragment.TAG)
                    .commit();

            mQuery = db.collection(User.COLLECTION_NAME)
                    .document(user.getUid())
                    .collection(FriendRequest.COLLECTION_NAME);

            adapter = new FriendRequestAdapter(mQuery, this) {
                @Override
                protected void onDataChanged() {
                    if (adapter.getItemCount() == 0) {
                        Log.d(REQUEST_TAG, "Empty requests");
                        tvNotification.setVisibility(View.GONE);
                        requestList.setVisibility(View.GONE);
                    } else {
                        Log.d(REQUEST_TAG, getItemCount() + " new requests");
                        tvNotification.setText(String.format(Locale.getDefault(),
                                "%d %s", adapter.getItemCount(),
                                getResources().getString(R.string.friend_request_notification)));
                        tvNotification.setVisibility(View.VISIBLE);
                        requestList.setVisibility(View.VISIBLE);
                    }
                }


                @Override
                protected void onError(FirebaseFirestoreException e) {
                    Log.d(REQUEST_TAG, "Error occurred");
                }
            };
            requestList.setLayoutManager(new LinearLayoutManager(getContext()));
            requestList.setAdapter(adapter);
            Log.d(REQUEST_TAG, "Adapter created");
        }
    }


    /**
     * Add sender to receiver's friend list.
     * Add receiver to sender's friend list.
     * Delete the request in receiver's request list.
     *
     * @param senderSnapshot sender.
     */
    @Override
    public void onFriendRequestAcceptedListener(final DocumentSnapshot senderSnapshot) {

        final DocumentReference receiverRef = db.collection(User.COLLECTION_NAME)
                .document(this.user.getUid());
        final DocumentReference senderRef = db.collection(User.COLLECTION_NAME)
                .document(senderSnapshot.getId());

        Toast.makeText(getContext(), senderSnapshot.getId(), Toast.LENGTH_SHORT).show();

        // Get sender in User Collection
        senderRef.get()
                .continueWith(new Continuation<DocumentSnapshot, Object>() {
                    @Override
                    public Object then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                        if (task.isSuccessful()) {
                            DocumentSnapshot fullSenderInfo = task.getResult();
                            final WriteBatch batch = db.batch();

                            if (fullSenderInfo.toObject(User.class) != null) {
                                // Add to receiver friend list
                                batch.set(receiverRef
                                                .collection(UserListFragment.COLLECTION_NAME)
                                                .document(fullSenderInfo.getId()),
                                        fullSenderInfo.toObject(User.class));
                            }
                            final String notification = String.format(Locale.getDefault(),
                                    "%s - ID %s is added to friend list",
                                    fullSenderInfo.getString(User.FIELD_NAME),
                                    fullSenderInfo.getId());
                            // Toast log
                            Toast.makeText(getContext(), notification, Toast.LENGTH_SHORT).show();

                            // Delete request
                            batch.delete(receiverRef.collection(FriendRequest.COLLECTION_NAME)
                                    .document(fullSenderInfo.getId()));

                            Log.d(REQUEST_TAG, "Request of "
                                    + fullSenderInfo.getString(User.FIELD_NAME) + " will be deleted");

                            receiverRef.get().continueWith(new Continuation<DocumentSnapshot, Void>() {
                                @Override
                                public Void then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                                    // Add receiver to sender's friend list
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot fullReceiverInfo = task.getResult();
                                        batch.set(senderRef.collection(UserListFragment.COLLECTION_NAME)
                                                .document(fullReceiverInfo.getId()), fullReceiverInfo.toObject(User.class));
                                        Toast.makeText(getContext(), String.format(Locale.getDefault(),
                                                "%s - ID %s is added to friend list",
                                                fullReceiverInfo.getString(User.FIELD_NAME),
                                                fullReceiverInfo.getId()), Toast.LENGTH_SHORT).show();
                                    }
                                    return batch.commit().getResult();
                                }
                            });

                            // Add receiver to sender friend list


                        } else {
                            Log.d(REQUEST_TAG, "Add friend failed");
                        }
                        return null;
                    }
                });
    }

    /**
     * Delete the request in receiver's request list
     *
     * @param senderSnapshot
     */

    @Override
    public void onFriendRequestRejectedListener(DocumentSnapshot senderSnapshot) {
        final DocumentReference receiverRef = db.collection(User.COLLECTION_NAME)
                .document(this.user.getUid());

        receiverRef.collection(FriendRequest.COLLECTION_NAME)
                .document(senderSnapshot.getId()).delete();

        Log.d(REQUEST_TAG, "Request of " + senderSnapshot.getString(User.FIELD_NAME) + " is deleted");
    }
}
