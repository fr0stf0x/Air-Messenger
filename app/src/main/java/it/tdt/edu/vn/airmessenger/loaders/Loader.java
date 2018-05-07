package it.tdt.edu.vn.airmessenger.loaders;

import android.support.v7.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

public abstract class Loader {

    private static MessageLoader messageLoader;
    private static UserLoader userLoader;
    protected CollectionReference colRef;
    protected DocumentReference docRef;

    public static Loader getUserLoader() {
        if (userLoader == null) {
            userLoader = UserLoader.getInstance();
        }
        return userLoader;
    }

    public static Loader getMessageLoader() {
        if (messageLoader == null) {
            messageLoader = MessageLoader.getInstance();
        }
        return messageLoader;
    }

    public Loader load(CollectionReference colRef) {
        this.colRef = colRef;
        return this;
    }

    public Loader load(DocumentReference docRef) {
        this.docRef = docRef;
        return this;
    }

    public Query query(String key, String value) {
        Query query = colRef.whereEqualTo(key, value);
        return query;
    }

    public abstract void into(RecyclerView recyclerView, int flag);

    public abstract void into(Object object);

    public abstract void getSingleObject();
}






