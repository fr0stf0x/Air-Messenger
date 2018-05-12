package it.tdt.edu.vn.airmessenger;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import it.tdt.edu.vn.airmessenger.adapters.UserAdapter;
import it.tdt.edu.vn.airmessenger.fragments.UserListFragment;
import it.tdt.edu.vn.airmessenger.models.User;

public class AllUsersActivity extends AppCompatActivity {

    public final String TAG = "AllUsersActivity";

    FirebaseFirestore db;
    FirebaseUser user;
    FirebaseAuth mAuth;
    FragmentManager fragmentManager;
    UserListFragment userListFragment;
    FragmentTransaction ft;
    Query mQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userListFragment = new UserListFragment();
        mQuery = db.collection(User.COLLECTION_NAME);
        user = mAuth.getCurrentUser();
        fragmentManager = getSupportFragmentManager();
        ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fragment_all_users, userListFragment, TAG).commit();
    }
}
