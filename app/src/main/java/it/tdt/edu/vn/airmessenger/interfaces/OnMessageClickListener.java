package it.tdt.edu.vn.airmessenger.interfaces;

import android.view.View;

import com.google.firebase.firestore.DocumentSnapshot;

public interface OnMessageClickListener {
    void onMessageClicked(int position);

    boolean onMessageLongClicked(int position);
}
