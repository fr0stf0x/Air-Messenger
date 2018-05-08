package it.tdt.edu.vn.airmessenger;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class UserInfoActivity extends AppCompatActivity {

    public static final String USER_ID_KEY = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
    }

}
