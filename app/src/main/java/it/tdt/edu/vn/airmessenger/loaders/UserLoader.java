package it.tdt.edu.vn.airmessenger.loaders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import it.tdt.edu.vn.airmessenger.models.User;

public class UserLoader extends Loader {
    final String TAG = "get_users";

    private static UserLoader instance;
    private static ArrayList<User> result;
    public static User user;

    static UserLoader getInstance() {
        if (instance == null) {
            instance = new UserLoader();
        }
        return instance;
    }

    @Override
    public void into(final RecyclerView recyclerView, final int flag) {
        if (colRef == null) {
            Log.d(TAG, "Null object reference");
            return;
        }
        if (result == null || !result.isEmpty()) {
            result = new ArrayList<>();
        }

        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot collection = task.getResult();
                    if (!collection.isEmpty()) {
                        for (DocumentSnapshot doc : collection) {
                            String userId = doc.getId();
                            String userName = doc.getString(User.FIELD_NAME);
                            String userEmail = doc.getString(User.FIELD_EMAIL);
                            String userStatus = doc.getString(User.FIELD_STATUS);
                            String userAvatar = doc.getString(User.FIELD_IMAGE);
                            String userThumbImg = doc.getString(User.FIELD_THUMB_IMAGE);


                        }
//                        UserAdapter adapter = new UserAdapter(result, flag);
//                        recyclerView.setAdapter(adapter);
                        Log.d(TAG, result.size() + " users fetched");
                    } else {
                        Log.d(TAG, "Empty collection");
                    }
                } else {
                    Log.d(TAG, "Failed");
                }
            }
        });
    }

    @Override
    public void into(final Object object) {
        if (object instanceof User) {
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Load single object successfully");
                        DocumentSnapshot doc = task.getResult();
                        String userName = doc.getString(User.FIELD_NAME);
                        String userEmail = doc.getString(User.FIELD_EMAIL);
                        String userStatus = doc.getString(User.FIELD_STATUS);
                        String userAvatar = doc.getString(User.FIELD_IMAGE);
                        String userThumbImg = doc.getString(User.FIELD_THUMB_IMAGE);

                        ((User) object).setName(userName);
                        ((User) object).setEmail(userEmail);
                        ((User) object).setStatus(userStatus);
                        ((User) object).setImage(userAvatar);
                        ((User) object).setThumbImage(userThumbImg);
                        Log.d(TAG, userName);
                    } else {
                        Log.d(TAG, "failed get single data");
                    }
                }
            });
        }
    }

    public void getSingleObject() {
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Load single object successfully");
                    DocumentSnapshot doc = task.getResult();
                    String userName = doc.getString(User.FIELD_NAME);
                    String userEmail = doc.getString(User.FIELD_EMAIL);
                    String userStatus = doc.getString(User.FIELD_STATUS);
                    String userAvatar = doc.getString(User.FIELD_IMAGE);
                    String userThumbImg = doc.getString(User.FIELD_THUMB_IMAGE);

                    Log.d(TAG, userName);
                } else {
                    Log.d(TAG, "failed get single data");
                }
            }
        });
    }
}
