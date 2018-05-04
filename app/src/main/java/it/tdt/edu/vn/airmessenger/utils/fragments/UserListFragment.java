package it.tdt.edu.vn.airmessenger.utils.fragments;

import android.content.Context;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import it.tdt.edu.vn.airmessenger.AllUsersActivity;
import it.tdt.edu.vn.airmessenger.MainActivity;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.interfaces.UserClickHandler;
import it.tdt.edu.vn.airmessenger.utils.adapters.UserAdapter;
import it.tdt.edu.vn.airmessenger.utils.models.User;

public class UserListFragment extends Fragment implements UserClickHandler {

    public static String COLLECTION_NAME = "friends";
    public static final int GET_USERS_FLAG = 1;
    public static final int GET_FRIENDS_FLAG = 0;
    private int flag = GET_FRIENDS_FLAG;

    FirebaseUser user;
    FirebaseFirestore db;

    CollectionReference colRef;
    ArrayList<User> userList;
    UserAdapter adapter;
    RecyclerView rvContacts;
    RecyclerView.LayoutManager layoutManager;

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
            setFlag(GET_USERS_FLAG);
        }
    }

    @Override
    public void onUserClick(int position) {
        Toast.makeText(getContext(), "Clicked " + userList.get(position).getDisplayName(), Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.contact_list_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (user != null) {
            rvContacts = view.findViewById(R.id.userList);
            userList = new ArrayList<>();
            layoutManager = new LinearLayoutManager(getActivity());
            rvContacts.setLayoutManager(layoutManager);
            fetchData();
        }
    }

    /*
    Fetch data from database into RecyclerView
     */
    private void fetchData() {
        switch (flag) {
            case GET_FRIENDS_FLAG:
                getAllFriends();
                break;
            case GET_USERS_FLAG:
                getAllUsers();
                break;
        }
    }

    private void getAllUsers() {
        colRef = db.collection(User.COLLECTION_NAME);
        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot userCollection = task.getResult();
                    if (!userCollection.isEmpty()) {

                        for (DocumentSnapshot userInfo : userCollection) {
                            String userName = userInfo.getString(User.FIELD_NAME);
                            String userStatus = userInfo.getString(User.FIELD_STATUS);
                            User newUser = new User(userName);
                            newUser.setStatus(userStatus);
                            userList.add(newUser);
                        }

                        adapter = new UserAdapter(userList, UserListFragment.this);
                        rvContacts.setAdapter(adapter);
                    }
                }
            }
        });
    }

    private void getAllFriends() {
        colRef = db.collection(User.COLLECTION_NAME).document(user.getUid()).collection(COLLECTION_NAME);

        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                final String TAG = "fetch_friends";
                if (task.isSuccessful()) {
                    QuerySnapshot collection = task.getResult();
                    if (!collection.isEmpty()) {

                        for (DocumentSnapshot document : collection.getDocuments()) {
                            String userName = document.getString(User.FIELD_NAME);
                            String userStatus = document.getString(User.FIELD_STATUS);
                            User newUser = new User(userName);
                            newUser.setStatus(userStatus);
                            userList.add(newUser);
                        }

                        Log.d(TAG, "Fetching success, " + userList.size() + " friends");

                        adapter = new UserAdapter(userList, UserListFragment.this);
                        rvContacts.setAdapter(adapter);

                        Log.d(TAG, userList.size() + " friends");

                    } else {
                        Log.d(TAG, "Fetching success, empty contacts");
                    }
                } else {
                    Log.d(TAG, "Fetching failed");
                }
            }
        });
    }

    private void setFlag(int flag) {
        this.flag = flag;
    }
}
