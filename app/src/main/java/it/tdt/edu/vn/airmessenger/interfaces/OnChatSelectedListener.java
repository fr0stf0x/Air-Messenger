package it.tdt.edu.vn.airmessenger.interfaces;

import com.google.firebase.firestore.DocumentSnapshot;

public interface OnChatSelectedListener {
    void onConversationClicked(DocumentSnapshot chat);
}