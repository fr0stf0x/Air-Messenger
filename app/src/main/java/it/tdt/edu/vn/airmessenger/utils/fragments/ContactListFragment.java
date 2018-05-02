package it.tdt.edu.vn.airmessenger.utils.fragments;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.utils.adapters.ContactAdapter;
import it.tdt.edu.vn.airmessenger.utils.models.User;

public class ContactListFragment extends Fragment {

    public static String COLLECTION_NAME = "friends";

    // TODO(3) HERREEE
    FirebaseUser user;
    FirebaseFirestore db;
    CollectionReference friendsRef;
    ArrayList<User> friendList;
    ContactAdapter adapter;
    RecyclerView rvContacts;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.contact_list_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvContacts = view.findViewById(R.id.list_contact);
        friendList = new ArrayList<>();
        fetchFriendList();

    }

    private void fetchFriendList() {
        friendsRef = db.collection(User.COLLECTION_NAME).document(user.getUid()).collection(COLLECTION_NAME);

        friendsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                final String TAG = "contact_fetch";
                if (task.isSuccessful()) {
                    QuerySnapshot collection = task.getResult();
                    if (!collection.isEmpty()) {
                        Log.d(TAG, "Fetching success, " + collection.size() + " friends");

                        for (DocumentSnapshot document : collection.getDocuments()) {
                            String userName = document.getString(User.FIELD_NAME);
                            String userStatus = document.getString(User.FIELD_STATUS);
                            User newUser = new User(userName);
                            newUser.setStatus(userStatus);
                            friendList.add(newUser);
                        }

                        rvContacts.setAdapter(adapter);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
                        rvContacts.setLayoutManager(layoutManager);

                    } else {
                        Log.d(TAG, "Fetching success, empty contacts");
                    }
                } else {
                    Log.d(TAG, "Fetching failed");
                    return;
                }
            }
        });
    }

}
