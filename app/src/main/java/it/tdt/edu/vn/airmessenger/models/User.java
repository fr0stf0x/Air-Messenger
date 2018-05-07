package it.tdt.edu.vn.airmessenger.models;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;

public class User {

    public static final String COLLECTION_NAME = "users";

    public static final String FIELD_ID = "user_id";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_IMAGE = "image";
    public static final String FIELD_THUMB_IMAGE = "thumb_image";

    private String userId;
    private String displayName;
    private String image;
    private String thumbImage;
    private String status;
    private String email;

    public User(String userId) {
        this.userId = userId;
    }

    public User(final String userId, String displayName, String email, String status, String image, String thumbImage) {
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
        this.image = image;
        this.thumbImage = thumbImage;
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

}