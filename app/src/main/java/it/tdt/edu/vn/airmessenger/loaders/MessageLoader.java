package it.tdt.edu.vn.airmessenger.loaders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import it.tdt.edu.vn.airmessenger.adapters.MessageAdapter;
import it.tdt.edu.vn.airmessenger.models.Message;

public class MessageLoader extends Loader {

    private static MessageLoader instance;
    private static ArrayList<Message> result;

    final String TAG = "get_messages";

    static MessageLoader getInstance() {
        if (instance == null) {
            instance = new MessageLoader();
        }
        return instance;
    }

    @Override
    public void into(final RecyclerView recyclerView, int flag) {
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
                            String sendUser = doc.getString(Message.FIELD_SEND_USER);
                            String receiveUser = doc.getString(Message.FIELD_RECEIVE_USER);
                            String content = doc.getString(Message.FIELD_CONTENT);
                            String time = doc.getString(Message.FIELD_TIME);

                            result.add(null);
                        }
//                        MessageAdapter adapter = new MessageAdapter(result);
//                        recyclerView.setAdapter(adapter);
                        Log.d(TAG, result.size() + " messages fetched");
                    } else {
                        Log.d(TAG, "Empty collection");
                    }
                } else {
                    Log.d(TAG, "Failed");
                }
            }
        });
    }

    // TODO edit here

    @Override
    public void into(Object object) {

    }

    @Override
    public void getSingleObject() {

    }
}

