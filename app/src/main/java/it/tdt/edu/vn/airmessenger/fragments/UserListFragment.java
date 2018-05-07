package it.tdt.edu.vn.airmessenger.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import it.tdt.edu.vn.airmessenger.AllUsersActivity;
import it.tdt.edu.vn.airmessenger.ChatActivity;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.loaders.UserLoader;
import it.tdt.edu.vn.airmessenger.interfaces.UserClickHandler;
import it.tdt.edu.vn.airmessenger.adapters.UserAdapter;
import it.tdt.edu.vn.airmessenger.loaders.Loader;
import it.tdt.edu.vn.airmessenger.models.User;

public class UserListFragment extends Fragment implements UserClickHandler {

    public static String COLLECTION_NAME = "friends";
    public static final int USERS_FLAG = 1;
    public static final int FRIENDS_FLAG = 0;
    private int flag = FRIENDS_FLAG;

    FirebaseUser user;
    FirebaseFirestore db;

    CollectionReference colRef;
    ArrayList<User> userList;
    RecyclerView rvContacts;
    RecyclerView.LayoutManager layoutManager;
    UserLoader loader;
    UserAdapter adapter;

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

        loader = (UserLoader) Loader.getUserLoader();

        if (context instanceof AllUsersActivity) {
            setFlag(USERS_FLAG);
        }
    }

    @Override
    public void onUserClick(int position) {

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
            layoutManager = new LinearLayoutManager(getActivity());
            rvContacts.setLayoutManager(layoutManager);

            switch (flag) {
                case FRIENDS_FLAG:
                    colRef = db.collection(User.COLLECTION_NAME)
                            .document(user.getUid()).collection(COLLECTION_NAME);
                    break;
                case USERS_FLAG:
                    colRef = db.collection(User.COLLECTION_NAME);
                    break;
            }
            adapter = (UserAdapter) rvContacts.getAdapter();
            loader.load(colRef).into(rvContacts, flag);

        }
    }

    private void setFlag(int flag) {
        this.flag = flag;
    }
}
