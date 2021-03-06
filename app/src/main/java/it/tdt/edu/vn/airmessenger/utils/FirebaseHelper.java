package it.tdt.edu.vn.airmessenger.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;
import it.tdt.edu.vn.airmessenger.App;
import it.tdt.edu.vn.airmessenger.models.User;

public class FirebaseHelper {

    public static final String TAG = "FirebaseHelper";

    public static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static StorageReference getStorage() {
        return FirebaseStorage.getInstance().getReference();
    }

    public static FirebaseFirestore getFirestore() {
        return FirebaseFirestore.getInstance();
    }

    public static void setFirebaseUserAvatar(final Uri imageFileUri) throws FirebaseException {
        StorageReference imageRef = getStorage()
                .child(getCurrentUser().getUid())
                .child(User.DATA_KEY)
                .child(User.FIELD_IMAGE + User.IMAGE_TYPE);

        imageRef.putFile(imageFileUri)
                .continueWith(new Continuation<UploadTask.TaskSnapshot, Uri>() {
                    // Get downloadUri from Firebase Storage
                    @Override
                    public Uri then(@NonNull Task<UploadTask.TaskSnapshot> task)
                            throws FirebaseException {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "then: get downloadUri completed");
                            return task.getResult().getDownloadUrl();
                        }
                        return null;
                    }
                })
                .continueWith(new Continuation<Uri, UserProfileChangeRequest>() {
                    // Create UpdateRequest
                    @Override
                    public UserProfileChangeRequest then(@NonNull Task<Uri> task)
                            throws FirebaseException {
                        if (task.isSuccessful()) {
                            Uri imgDownUrl = task.getResult();
                            Log.d(TAG, "then: create UpdateRequest completed");
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest
                                    .Builder()
                                    .setPhotoUri(imgDownUrl)
                                    .build();
                            return profileUpdates;
                        }
                        return null;
                    }
                })
                .continueWith(new Continuation<UserProfileChangeRequest, Void>() {
                    // Operate UpdateRequest
                    @Override
                    public Void then(@NonNull Task<UserProfileChangeRequest> task)
                            throws FirebaseException {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "then: Operate UpdateRequest completed");
                            final UserProfileChangeRequest profileUpdates = task.getResult();
                            getCurrentUser().updateProfile(profileUpdates)
                                    .continueWith(new Continuation<Void, Void>() {
                                        // Operate Update in database
                                        @Override
                                        public Void then(@NonNull Task<Void> task) throws Exception {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "then: Success, now upload thumb");
                                                setFirestoreImages(imageFileUri, profileUpdates.getPhotoUri());
                                            }
                                            return null;
                                        }
                                    });
                        }
                        return null;
                    }
                });
    }

    public static void setFirestoreImages(final Uri localImageUri, final Uri cloudImageUri) {
        final String TAG = "Set_thumb_image";

        StorageReference thumbRef = getStorage().child(getCurrentUser().getUid())
                .child(User.DATA_KEY).child(User.FIELD_THUMB_IMAGE + User.IMAGE_TYPE);

        File thumb_file = new File(localImageUri.getPath());
        Bitmap thumbnailBitmap = null;
        Log.d(TAG, "setThumbImage: init");

        try {
            thumbnailBitmap = new Compressor(App.getContext())
                    .setMaxHeight(200)
                    .setMaxWidth(200)
                    .setQuality(75)
                    .compressToBitmap(thumb_file);
            Log.d(TAG, "setThumbImage: compress image successfully");
            if (thumbnailBitmap == null) {
                Log.d(TAG, "setThumbImage: FAILED");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (thumbnailBitmap != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Log.d(TAG, "setThumbImage: ByteArray created");
            thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            byte[] data = outputStream.toByteArray();

            thumbRef.putBytes(data)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                UploadTask.TaskSnapshot taskSnapshot = task.getResult();
                                Uri thumbDownUrl = taskSnapshot.getDownloadUrl();
                                Map<String, Object> updates = new HashMap<>();
                                if (thumbDownUrl != null) {
                                    Log.d(TAG, "then: ThumbDownURL: " + thumbDownUrl.toString());
                                    Log.d(TAG, "onComplete: ImageDownURL " + cloudImageUri.toString());
                                    updates.put(User.FIELD_THUMB_IMAGE,
                                            thumbDownUrl.toString());
                                    updates.put(User.FIELD_IMAGE, cloudImageUri.toString());

                                    getFirestore().collection(User.COLLECTION_NAME)
                                            .document(getCurrentUser().getUid())
                                            .update(updates)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "onSuccess: Update completed");
                                                    } else {
                                                        Log.d(TAG, "onComplete: Update failed");
                                                    }
                                                }
                                            })
                                            .addOnCanceledListener(new OnCanceledListener() {
                                                @Override
                                                public void onCanceled() {
                                                    Log.d(TAG, "onCanceled: Update canceled");
                                                }
                                            });
                                } else {
                                    Log.d(TAG, "onComplete: Error getting thumb image download url");
                                }
                            } else {
                                Log.d(TAG, "onComplete: Error uploading thumb image to FirebaseStorage");
                            }
                        }
                    });
        }
    }
}
