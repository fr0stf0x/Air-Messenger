package it.tdt.edu.vn.airmessenger;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.tdt.edu.vn.airmessenger.adapters.MainPagerAdapter;
import it.tdt.edu.vn.airmessenger.models.User;

public class MainActivity extends AppCompatActivity {

    final int CONVERSATION_PAGE = 0;
    final int CONTACT_PAGE = 1;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @BindView(R.id.tabLayout)
    TabLayout tabLayout;

    @BindView(R.id.viewPager)
    ViewPager viewPager;

    @BindView(R.id.fab_new_contact)
    FloatingActionButton fabNewContact;

    @BindView(R.id.fab_new_message)
    FloatingActionButton fabNewMessage;

    boolean doubleBackToExitPressedOnce = false;

    MainPagerAdapter pagerAdapter;

    // for future use
    DocumentSnapshot userSnapshot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }

        ButterKnife.bind(this);
        pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        setupFloatingActionButton();

        updateUI();
    }

    private void setupFloatingActionButton() {
        fabNewMessage.show();
        fabNewContact.hide();
        setupFabAnimation();
        setupFabAction();
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            int currentPage = savedInstanceState.getInt(MainPagerAdapter.CURRENT_PAGE_NUMBER_KEY);
            viewPager.setCurrentItem(currentPage);
            Log.d("Get " + MainPagerAdapter.CURRENT_PAGE_NUMBER_KEY, currentPage + "");
        }

    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getResources().getString(R.string.back_again_to_exit), Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        } else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }

    }

    private boolean hasUserSignedIn() {
        user = mAuth.getCurrentUser();
        if (user.getPhotoUrl() != null) {
            Log.d("Get_user_info", "Photo URL: " + user.getPhotoUrl().toString());
        }
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
        final String TAG = "Get user info";
        db.collection("users").document(user.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    userSnapshot = task.getResult();
                    if (userSnapshot.exists()) {
                        if (user == null) {
                            return;
                        }
                        Log.d(TAG, userSnapshot.getId() + " is fetched");
                        String toastMsg = String.format(Locale.getDefault(),
                                getResources().getString(R.string.sign_old_notification),
                                user.getDisplayName());
                        Toast.makeText(MainActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                        ActionBar actionBar = getSupportActionBar();
                        actionBar.setTitle(user.getDisplayName());
                    } else {
                        initUser();
                    }
                } else {
                    Log.d(TAG, "Error get user info");
                }
            }
        });
    }


    /*
    Init UserInfo in Firebase Firestore
     */
    private void initUser() {
        CollectionReference users = db.collection("users");
        HashMap<String, Object> userInfo = User.initUser(user, this);
        String toastMsg = String.format(Locale.getDefault(),
                getResources().getString(R.string.sign_new_notification),
                user.getDisplayName(),
                getResources().getString(R.string.app_name));

        Toast.makeText(MainActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
        users.document(user.getUid()).set(userInfo); // Set REF
    }

    private void backToWelcomeScreen() {
        Intent intent = new Intent(this, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MainPagerAdapter.CURRENT_PAGE_NUMBER_KEY, viewPager.getCurrentItem());
        Log.d("Set " + MainPagerAdapter.CURRENT_PAGE_NUMBER_KEY, viewPager.getCurrentItem() + "");
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
                Intent intent = new Intent(MainActivity.this, UserInfoActivity.class);
                intent.putExtra(User.USER_ID_KEY, user.getUid());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupFabAction() {
        fabNewContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AllUsersActivity.class);
                startActivity(intent);
            }
        });

        fabNewMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(CONTACT_PAGE);
            }
        });
    }

    private void setupFabAnimation() {
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