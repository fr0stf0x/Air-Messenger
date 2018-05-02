package it.tdt.edu.vn.airmessenger.utils.models;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;

public class User {

    private String displayName;
    private String image;
    private String thumbImage;
    private String status;
    private String email;

    public static String COLLECTION_NAME = "users";
    public static String FIELD_EMAIL = "email";
    public static String FIELD_NAME = "name";
    public static String FIELD_STATUS = "status";

    public User(String displayName) {
        this.displayName = displayName;
    }

    public User(String displayName, String email, String image, String thumbImage, String status) {
        this.displayName = displayName;
        this.email = email;
        this.image = image;
        this.thumbImage = thumbImage;
        this.status = status;
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

    public void addContact() {

    }

}
