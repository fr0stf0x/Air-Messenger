package it.tdt.edu.vn.airmessenger.models;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.HashMap;

import it.tdt.edu.vn.airmessenger.App;
import it.tdt.edu.vn.airmessenger.R;

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
        int age = 0;

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
}