package it.tdt.edu.vn.airmessenger.models;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public static final String COLLECTION_NAME = "users";
    public static final String USER_ID_KEY = "userId";

    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_IMAGE = "image";
    public static final String FIELD_THUMB_IMAGE = "thumbImage";
    public static final String FIELD_SEX = "sex";
    public static final String FIELD_AGE = "age";

    private String name;
    private String image;
    private String thumbImage;
    private String status;
    private String email;
    private int age;
    private String sex;

    public User() {
    }

    public User(String name, String email, String status, String image, String thumbImage, String sex, int age) {
        this.name = name;
        this.email = email;
        this.image = image;
        this.thumbImage = thumbImage;
        this.status = status;
        this.sex = sex;
        this.age = age;
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
}