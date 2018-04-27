package it.tdt.edu.vn.airmessenger.utils.models;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import java.util.List;

public class Conversation extends FragmentActivity {

    String firstUser;
    String secondUser;
    String conversationID;
    List<Message> messageList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
