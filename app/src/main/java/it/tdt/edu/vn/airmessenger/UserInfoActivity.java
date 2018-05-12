package it.tdt.edu.vn.airmessenger;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import it.tdt.edu.vn.airmessenger.models.FriendRequest;
import it.tdt.edu.vn.airmessenger.models.User;

public class UserInfoActivity extends AppCompatActivity {

    public static final String TAG = "UserInfo";
    public static final int USER_FLAG = 0;
    public static final int SELF_FLAG = 1;
    private static final int CAMERA_REQUEST = 1888;
    static final int REQUEST_TAKE_PHOTO = 1;

    private int flag = USER_FLAG;

    @BindView(R.id.imgUserAvatar)
    CircleImageView imgUserAvatar;

    @BindView(R.id.btn_add_friend)
    Button btnAddFriend;

    @BindView(R.id.tvUsername)
    TextView tvUsername;

    @BindView(R.id.tvStatus)
    TextView tvStatus;

    @BindView(R.id.tvAge)
    TextView tvAge;

    @BindView(R.id.tvSex)
    TextView tvSex;

    FirebaseUser firebaseUser;
    FirebaseStorage storage;

    User refUser;
    FirebaseFirestore db;
    String refUserId;

    private Bitmap photo;

    String mCurrentPhotoPath;
    private Uri photoURI = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        db = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();

        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent != null) {
            refUserId = intent.getStringExtra(User.USER_ID_KEY);
            if (!refUserId.equals("") || refUserId == null) {
                bindUserInfo(refUserId);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d(TAG, "Can not take photo");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "it.tdt.edu.vn.airmessenger",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            this.photo = (Bitmap) data.getExtras().get("data");
        }

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            galleryAddPic();
            setPic();
            setFirebaseUserAvatar();
        }
    }

    private void setFirebaseUserAvatar() {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(photoURI).build();
        firebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                        }
                        else {
                            Log.d(TAG, "Error");
                        }
                    }
                });
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = imgUserAvatar.getWidth();
        int targetH = imgUserAvatar.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        imgUserAvatar.setImageBitmap(bitmap);
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
                            tvAge.setText(String.valueOf(refUser.getAge()));
                            tvSex.setText(refUser.getSex());
                            tvStatus.setText(refUser.getStatus());
                            tvUsername.setText(refUser.getName());
                            // TODO HERE
                            if (firebaseUser.getPhotoUrl() != null) {
                                Picasso.get()
                                        .load(firebaseUser.getPhotoUrl())
                                        .into(imgUserAvatar);
                            }

                            if (userId.equals(firebaseUser.getUid())) {
                                btnAddFriend.setVisibility(View.GONE);
                                imgUserAvatar.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dispatchTakePictureIntent();
                                    }
                                });
                            }

                            btnAddFriend.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    sendAddRequest(fullUserInfo);
                                }
                            });
                        }
                    }
                });
    }

    /*
    Add a request in receiver's request list
     */
    private void sendAddRequest(final DocumentSnapshot receiverUserSnapshot) {
        Log.d(TAG, "Send request triggered");

        // Get receiver reference to get the name
        db.collection(User.COLLECTION_NAME)
                .document(firebaseUser.getUid()).get()
                .continueWith(new Continuation<DocumentSnapshot, Void>() {
                    @Override
                    public Void then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                        if (task.isSuccessful()) {
                            DocumentSnapshot senderInfo = task.getResult();
                            Date sendTime = Calendar.getInstance().getTime();
                            HashMap<String, Object> request = new HashMap<>();

                            request.put(FriendRequest.FIELD_USER_NAME,
                                    senderInfo.getString(User.FIELD_NAME));
                            request.put(FriendRequest.FIELD_SEND_TIME, sendTime);

                            db.collection(User.COLLECTION_NAME)
                                    .document(receiverUserSnapshot.getId())
                                    .collection(FriendRequest.COLLECTION_NAME)
                                    .document(senderInfo.getId())
                                    .set(request);

                            btnAddFriend.setText(getResources().getString(R.string.button_add_friend_disabled));
                            btnAddFriend.setEnabled(false);
                        } else {
                            Log.d(TAG, "Error happened");
                        }
                        return null;
                    }
                });
    }


}
