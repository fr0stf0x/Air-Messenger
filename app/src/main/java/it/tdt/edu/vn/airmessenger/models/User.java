package it.tdt.edu.vn.airmessenger.models;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;

import it.tdt.edu.vn.airmessenger.App;
import it.tdt.edu.vn.airmessenger.R;
import it.tdt.edu.vn.airmessenger.UserInfoActivity;
import it.tdt.edu.vn.airmessenger.fragments.UserListFragment;
import it.tdt.edu.vn.airmessenger.utils.FirebaseHelper;

@IgnoreExtraProperties
public class User {

    public static final String COLLECTION_NAME = "users";

    public static final String USER_ID_KEY = "userId";
    public static final String DATA_KEY = "data";

    public static final String IMAGE_TYPE = ".jpg";

    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_PHONE = "phoneNumber";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_IMAGE = "image";
    public static final String FIELD_THUMB_IMAGE = "thumbImage";
    public static final String FIELD_SEX = "sex";
    public static final String FIELD_AGE = "age";
    public static final String FIELD_CHAT_WITH = "with";

    private String name;
    private String image;
    private String phoneNumber;
    private String thumbImage;
    private String status;
    private String email;
    private int age;
    private String sex;

    public User() {

    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumbImage() {
        return thumbImage;
    }

    public void setThumbImage(String thumbImage) {
        this.thumbImage = thumbImage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;

    }

    /**
     * Initialize a User-Map object from a {@link FirebaseUser} object
     */
    public static HashMap<String, Object> initUser(FirebaseUser user) {
        HashMap<String, Object> userInfo = new HashMap<>();
        String name = (user.getDisplayName().equals("") || user.getDisplayName() == null) ?
                App.getContext().getResources().getString(R.string.default_username) : user.getDisplayName();
        String email = (user.getEmail().equals("") || user.getEmail() == null) ?
                App.getContext().getResources().getString(R.string.default_email) : user.getEmail();
        String phoneNumber = (user.getPhoneNumber() == null || user.getPhoneNumber().equals("")) ?
                App.getContext().getResources().getString(R.string.default_phone_number) : user.getPhoneNumber();
        String thumbImage = (user.getPhotoUrl() == null) ?
                App.getContext().getResources().getString(R.string.default_thumb_image) : user.getPhotoUrl().toString();
        String image = (user.getPhotoUrl() == null) ?
                App.getContext().getResources().getString(R.string.default_image) : user.getPhotoUrl().toString();
        String status = App.getContext().getResources().getString(R.string.default_status);
        String sex = App.getContext().getResources().getString(R.string.default_status);
        int age = App.getContext().getResources().getInteger(R.integer.default_age);

        userInfo.put(User.FIELD_NAME, name);
        userInfo.put(User.FIELD_EMAIL, email);
        userInfo.put(User.FIELD_STATUS, status);
        userInfo.put(User.FIELD_AGE, age);
        userInfo.put(User.FIELD_SEX, sex);
        userInfo.put(User.FIELD_PHONE, phoneNumber);
        userInfo.put(User.FIELD_THUMB_IMAGE, thumbImage);
        userInfo.put(User.FIELD_IMAGE, image);
        return userInfo;
    }

    public static void updateUser(String userId, User user) {
        HashMap<String, Object> userInfo = new HashMap<>();
        userInfo.put(User.FIELD_NAME, user.getName());
        userInfo.put(User.FIELD_STATUS, user.getStatus());
        userInfo.put(User.FIELD_EMAIL, user.getEmail());
        userInfo.put(User.FIELD_AGE, user.getAge());
        userInfo.put(User.FIELD_SEX, user.getSex());
        userInfo.put(User.FIELD_PHONE, user.getPhoneNumber());
        FirebaseHelper.getFirestore()
                .collection(User.COLLECTION_NAME)
                .document(userId)
                .update(userInfo)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(App.getContext(), App.getContext()
                                    .getString(R.string.update_success), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(App.getContext(), App.getContext()
                                    .getString(R.string.update_fail), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public static Task<Void> unfriend(final String friendId) {
        FirebaseFirestore db = FirebaseHelper.getFirestore();
        FirebaseUser firebaseUser = FirebaseHelper.getCurrentUser();
        WriteBatch batch = db.batch();
        return batch.delete(db.collection(User.COLLECTION_NAME).document(firebaseUser.getUid()).collection(UserListFragment.COLLECTION_NAME).document(friendId))
                .delete(db.collection(User.COLLECTION_NAME).document(friendId).collection(UserListFragment.COLLECTION_NAME).document(firebaseUser.getUid()))
                .commit();
    }
}