package it.tdt.edu.vn.airmessenger;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.TooltipCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import it.tdt.edu.vn.airmessenger.fragments.UserListFragment;
import it.tdt.edu.vn.airmessenger.models.FriendRequest;
import it.tdt.edu.vn.airmessenger.models.User;
import it.tdt.edu.vn.airmessenger.utils.FirebaseHelper;

/**
 * This class do 2 things:
 * <ol>
 * <li>Display this user's info.</li>
 * <li>Display full UserInfo when user click on another user.</li>
 * </ol>
 * When in option 1, user can edit his/her information.
 * When in option 2, user can add friend by click button add friend.
 *
 * @see #btnFunction
 */

public class UserInfoActivity extends AppCompatActivity {

    public static final String TAG = "UserInfo";
    public static final int USER_FLAG = 0;
    public static final int SELF_FLAG = 1;

    public static final int TYPE_SELF = 0;
    public static final int TYPE_UNKNOWN = 1;
    public static final int TYPE_FRIEND = 2;
    public static final int TYPE_SENT_REQUEST = 3;

    private int flag = USER_FLAG;

    @BindView(R.id.layout)
    ConstraintLayout layout;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.imgUserAvatar)
    CircleImageView imgUserAvatar;

    @BindView(R.id.btn_add_friend)
    Button btnFunction;

    @BindView(R.id.tvUsername)
    TextView tvUsername;

    @BindView(R.id.tvPhone)
    TextView tvPhone;

    @BindView(R.id.tvEmail)
    TextView tvEmail;

    @BindView(R.id.tvStatus)
    TextView tvStatus;

    @BindView(R.id.tvAgeAndSex)
    TextView tvAgeAndSex;

    FirebaseUser firebaseUser;
    StorageReference storage;
    User refUser;
    FirebaseFirestore db;
    String refUserId;
    int type = TYPE_SELF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        db = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance().getReference();

        ButterKnife.bind(this);
    }

    private void setupButton() {
        switch (type) {
            case TYPE_UNKNOWN:
                btnFunction.setText(getString(R.string.button_add_friend));
                setButtonBackground(true);
                btnFunction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FriendRequest.sendRequest(firebaseUser.getUid(), firebaseUser.getDisplayName(), refUserId)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            type = TYPE_SENT_REQUEST;
                                            setupButton();
                                        }
                                    }
                                });
                    }
                });
                break;
            case TYPE_SENT_REQUEST:
                btnFunction.setText(getString(R.string.button_add_friend_disabled));
                setButtonBackground(false);
                btnFunction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FriendRequest.undoSendRequest(firebaseUser.getUid(), refUserId)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            type = TYPE_UNKNOWN;
                                            setupButton();
                                        }
                                    }
                                });
                    }
                });
                break;
            case TYPE_FRIEND:
                btnFunction.setText(getString(R.string.already_friend));
                setButtonBackground(false);
                btnFunction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        User.unfriend(refUserId).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    type = TYPE_UNKNOWN;
                                    setupButton();
                                }
                            }
                        });
                    }
                });
            default:
                break;

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        if (intent != null) {
            refUserId = intent.getStringExtra(User.USER_ID_KEY);
        }
        if (refUserId == null || refUserId.equals("")) {
            refUserId = firebaseUser.getUid();
        }
        bindUserInfo(refUserId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Picasso.get().load(resultUri).into(imgUserAvatar);
                Log.d(TAG, "onActivityResult: Now upload picture");
                try {
                    FirebaseHelper.setFirebaseUserAvatar(resultUri);
                } catch (FirebaseException e) {
                    Log.d(TAG, "Error: " + e.getMessage());
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    private void bindUserInfo(final String userId) {
        db.collection(User.COLLECTION_NAME).document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            final DocumentSnapshot fullUserInfo = task.getResult();
                            refUser = fullUserInfo.toObject(User.class);
                            if (refUser == null) {
                                Log.d(TAG, "Error");
                                finish();
                            }
                            // TODO(DONE) SET age and sex
                            tvAgeAndSex.setText(String.format(Locale.getDefault(), "%s, at %s",
                                    refUser.getSex(),
                                    String.valueOf(refUser.getAge())
                            ));

                            tvStatus.setText(refUser.getStatus());

                            tvUsername.setText(refUser.getName());

                            if (refUser.getImage() != null) {
                                Log.d(TAG, "onComplete: " + refUser.getImage());
                                Picasso.get()
                                        .load(refUser.getImage())
                                        .placeholder(R.drawable.man_icon)
                                        .into(imgUserAvatar);
                            }

                            tvEmail.setText("Email: " + refUser.getEmail());

                            tvPhone.setText("Tel: " + refUser.getPhoneNumber());


                            if (userId.equals(firebaseUser.getUid())) {
                                TooltipCompat.setTooltipText(imgUserAvatar, "Click me to change your avatar");
                                imgUserAvatar.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CropImage.activity()
                                                .setActivityTitle(getResources().getString(R.string.activity_crop_image_label))
                                                .setCropShape(CropImageView.CropShape.OVAL)
                                                .setAspectRatio(1, 1)
                                                .setGuidelines(CropImageView.Guidelines.ON)
                                                .start(UserInfoActivity.this);
                                    }
                                });
                                btnFunction.setText(getResources().getString(R.string.button_edit_info));
                                btnFunction.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        editUserInfo();
                                    }
                                });
                            } else {
                                // kiem tra co phai la ban be hay chua
                                db.collection(User.COLLECTION_NAME)
                                        .document(firebaseUser.getUid())
                                        .collection(UserListFragment.COLLECTION_NAME)
                                        .document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if (task.getResult().exists()) {
                                                final DocumentSnapshot friend = task.getResult();
                                                type = TYPE_FRIEND;
                                                setupButton();

                                            } else {
                                                // Kiem tra co goi loi moi ket ban hay chua
                                                db.collection(User.COLLECTION_NAME)
                                                        .document(userId)
                                                        .collection(FriendRequest.COLLECTION_NAME)
                                                        .document(firebaseUser.getUid())
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    if (task.getResult().exists()) {
                                                                        type = TYPE_SENT_REQUEST;
                                                                        setupButton();
                                                                    }
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    }
                                });
                            }
                            hideProgressBar();
                        }
                    }
                });
    }


    private void setButtonBackground(boolean enable) {
        if (!enable)
            btnFunction.setBackgroundColor(getResources().getColor(R.color.error));
        else {
            btnFunction.setBackgroundColor(getResources().getColor(R.color.secondaryDarkColor_Light));
        }
    }

    private void editUserInfo() {
        Intent intent = new Intent(UserInfoActivity.this
                , UserEditActivity.class);
        startActivity(intent);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        layout.setVisibility(View.VISIBLE);
    }
}
