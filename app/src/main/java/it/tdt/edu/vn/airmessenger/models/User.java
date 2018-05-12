package it.tdt.edu.vn.airmessenger.models;

import android.content.Context;
import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.HashMap;

import it.tdt.edu.vn.airmessenger.R;

@IgnoreExtraProperties
public class User {

    public static final String COLLECTION_NAME = "users";
    public static final String USER_ID_KEY = "userId";
    public static final String DATA_KEY = "data";

    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_PHONE = "phoneNumber";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_IMAGE = "image";
    public static final String FIELD_THUMB_IMAGE = "thumbImage";
    public static final String FIELD_SEX = "sex";
    public static final String FIELD_AGE = "age";

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

    /*
    Initialize a User-Map object from a FirebaseUser instance
     */
    public static HashMap<String, Object> initUser(FirebaseUser user, Context context) {
        HashMap<String, Object> userInfo = new HashMap<>();
        // Check name
        String name = (user.getDisplayName().equals("") || user.getDisplayName() == null) ?
                context.getResources().getString(R.string.default_username) : user.getDisplayName();
        // Check email
        String email = (user.getEmail().equals("") || user.getEmail() == null) ?
                context.getResources().getString(R.string.default_email) : user.getEmail();
        // Check phonenumber
        String phoneNumber = (user.getPhoneNumber() == null || user.getPhoneNumber().equals("")) ?
                context.getResources().getString(R.string.default_phone_number) : user.getPhoneNumber();
        // Check user photo exists
        String thumbImage = (user.getPhotoUrl() == null) ?
                context.getResources().getString(R.string.default_thumb_image) : user.getPhotoUrl().toString();
        String image = (user.getPhotoUrl() == null) ?
                context.getResources().getString(R.string.default_image) : user.getPhotoUrl().toString();


        String status = context.getResources().getString(R.string.default_status);
        int age = 0;
        String sex = context.getResources().getString(R.string.default_status);

        userInfo.put(User.FIELD_NAME, name);
        userInfo.put(User.FIELD_EMAIL, email);
        userInfo.put(User.FIELD_STATUS, status);
        userInfo.put(User.FIELD_AGE, age);
        userInfo.put(User.FIELD_SEX, context.getResources()
                .getString(R.string.default_sex));
        userInfo.put(User.FIELD_PHONE, phoneNumber);
        userInfo.put(User.FIELD_THUMB_IMAGE, thumbImage);
        userInfo.put(User.FIELD_IMAGE, image);
        return userInfo;
    }
}