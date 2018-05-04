package it.tdt.edu.vn.airmessenger;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import it.tdt.edu.vn.airmessenger.utils.adapters.MainPagerAdapter;
import it.tdt.edu.vn.airmessenger.utils.models.User;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    MainPagerAdapter adapter;
    FloatingActionButton fabNewContact;
    FloatingActionButton fabNewMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        fabNewContact = findViewById(R.id.fab_new_contact);
        fabNewMessage = findViewById(R.id.fab_new_message);

        adapter = new MainPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        setUpFabAnimate();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }
        updateUI();
    }


    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    private boolean hasUserSignedIn() {
        user = mAuth.getCurrentUser();
        return user != null;
    }


    private void updateUI() {
        if (!hasUserSignedIn()) {
            backToWelcomeScreen();
        } else {
            getUserInfoIfExist();
        }
    }

    private void getUserInfoIfExist() {
        DocumentReference docRef =
                db.collection("users").document(user.getUid());

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                final String TAG = "db_fetch";
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // User already exists
                        Log.d(TAG, "Success! " + document.getId());
                    } else {
                        initUser();
                    }
                } else {
                    Log.d(TAG, "Failed! ");
                }
            }
        });
    }

    /*
    Init UserInfo in Firebase Firestore
     */
    private void initUser() {
        CollectionReference users = db.collection("users");
        HashMap<String, Object> userInfo = new HashMap<>();
        userInfo.put(User.FIELD_NAME, user.getDisplayName() == null ?
                getResources().getString(R.string.default_username) :
                user.getDisplayName());
        userInfo.put(User.FIELD_EMAIL, user.getEmail() == null ?
                getResources().getString(R.string.default_email) :
                user.getEmail());
        userInfo.put(User.FIELD_STATUS,
                getResources().getString(R.string.default_status));
        users.document(user.getUid()).set(userInfo);
        userInfo.put(User.FIELD_THUMB_IMAGE,
                getResources().getString(R.string.default_thumb_image));
        users.document(user.getUid()).set(userInfo);
        userInfo.put(User.FIELD_IMAGE,
                getResources().getString(R.string.default_image));
        users.document(user.getUid()).set(userInfo);
    }

    private void backToWelcomeScreen() {
        Intent intent = new Intent(this, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /*
    Sign out with Firebase AuthUI
     */
    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this, "You've successfully logged out", Toast.LENGTH_SHORT).show();
                        updateUI();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_log_out:
                signOut();
                return true;
            case R.id.action_settings:
//                Toast.makeText(this, "Home Settings Click", Toast.LENGTH_SHORT).show();
                // Testing
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpFabAnimate() {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                animateFab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                animateFab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void animateFab(int position) {
        switch (position) {
            case 0:
                fabNewMessage.show();
                fabNewContact.hide();
                break;
            case 1:
                fabNewContact.show();
                fabNewMessage.hide();
                break;
            default:
                fabNewMessage.show();
                fabNewContact.hide();
                break;
        }
    }
}

// TODO(5) get icon, message images
// TODO(6) build a welcome activity



