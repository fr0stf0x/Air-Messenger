package it.tdt.edu.vn.airmessenger.interfaces;

import com.google.firebase.firestore.DocumentSnapshot;

public interface OnMessageClickListener {
    void onMessageClicked(DocumentSnapshot msg);
}
