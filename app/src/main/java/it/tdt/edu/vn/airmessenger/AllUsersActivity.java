package it.tdt.edu.vn.airmessenger;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import it.tdt.edu.vn.airmessenger.fragments.UserListFragment;
import it.tdt.edu.vn.airmessenger.loaders.Loader;
import it.tdt.edu.vn.airmessenger.loaders.UserLoader;

public class AllUsersActivity extends AppCompatActivity {

    final String TAG = "AllUsersActivity";

    UserLoader loader;
    FirebaseFirestore db;
    CollectionReference colRef;
    FirebaseUser user;
    FirebaseAuth mAuth;
    RecyclerView rvUsers;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        ActionBar actionBar = getSupportActionBar();

        try {
            actionBar.setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            Log.d("ActionBarSetting", e.getMessage());
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        rvUsers = findViewById(R.id.rvUsers);
        layoutManager = new LinearLayoutManager(this);
        rvUsers.setLayoutManager(layoutManager);

        user = mAuth.getCurrentUser();
        if (user != null) {
            loader = (UserLoader) Loader.getUserLoader();
            colRef = db.collection("users");
            loader.load(colRef).into(rvUsers, UserListFragment.USERS_FLAG);
        }
    }
}
