package it.tdt.edu.vn.airmessenger;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;
import it.tdt.edu.vn.airmessenger.models.User;
import it.tdt.edu.vn.airmessenger.utils.FirebaseHelper;

public class UserEditActivity extends AppCompatActivity {

    FirebaseFirestore db;
    FirebaseUser firebaseUser;
    User user;

    @BindView(R.id.edtName)
    EditText edtName;

    @BindView(R.id.edtPhone)
    EditText edtPhone;

    @BindView(R.id.edtAge)
    EditText edtAge;

    @BindView(R.id.spinnerSex)
    Spinner spinnerSex;
    ArrayAdapter<CharSequence> sexAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit);

        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseHelper.getCurrentUser();

        sexAdapter = ArrayAdapter.createFromResource(this, R.array.sex, android.R.layout.simple_spinner_item);
        sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerSex.setAdapter(sexAdapter);
        bindUserInfo();
    }

    @OnItemSelected(R.id.spinnerSex)
    public void onSexSelected(int position) {
        Toast.makeText(this, "selected" + sexAdapter.getItem(position), Toast.LENGTH_SHORT).show();
    }

    private void bindUserInfo() {
        db.collection(User.COLLECTION_NAME)
                .document(firebaseUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            user = task.getResult().toObject(User.class);
                            if (user != null) {
                                edtName.setHint(user.getName());
                                edtAge.setHint(String.valueOf(user.getAge()));
                                edtPhone.setHint(user.getPhoneNumber());
                            }
                        }
                    }
                });
    }
}
