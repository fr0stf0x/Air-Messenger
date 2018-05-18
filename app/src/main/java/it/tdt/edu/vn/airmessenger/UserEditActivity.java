package it.tdt.edu.vn.airmessenger;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.TooltipCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import it.tdt.edu.vn.airmessenger.models.User;
import it.tdt.edu.vn.airmessenger.utils.FirebaseHelper;

public class UserEditActivity extends AppCompatActivity {

    public static final String TAG = "UserEditActivity";

    FirebaseFirestore db;
    FirebaseUser firebaseUser;
    User user;

    @BindView(R.id.edtName)
    EditText edtName;

    @BindView(R.id.edtPhone)
    EditText edtPhone;

    @BindView(R.id.edtEmail)
    EditText edtEmail;

    @BindView(R.id.edtStatus)
    EditText edtStatus;

    @BindView(R.id.edtAge)
    EditText edtAge;

    @BindView(R.id.spinnerSex)
    Spinner spinnerSex;

    @BindView(R.id.btnSubmit)
    Button btnSubmit;

    @BindString(R.string.dialog_set_age_label)
    String dialogLabel;

    @BindString(R.string.tooltip_cannot_set_email)
    String emailTooltipText;

    NumberPicker numberPicker;

    String name;
    String phone;
    String sex;
    String status;
    String email;
    int age = 0;

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
        sex = sexAdapter.getItem(position).toString();
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
                                name = user.getName();
                                email = user.getEmail();
                                age = user.getAge();
                                phone = user.getPhoneNumber();
                                sex = user.getSex();
                                status = user.getStatus();
                                resetInfo();
                            }
                        }
                    }
                });
    }

    @OnClick(R.id.btnSetAge)
    public void openNumberPicker() {
        final Dialog dialog = new Dialog(this);
        dialog.setTitle(dialogLabel);
        dialog.setContentView(R.layout.dialog_age_pick);
        numberPicker = dialog.findViewById(R.id.agePicker);
        // Tuoi tu 12 den 65 :)
        numberPicker.setMaxValue(65);
        numberPicker.setMinValue(12);
        numberPicker.setValue(age);
        numberPicker.setWrapSelectorWheel(false);
        Button btnSubmit, btnCancel;
        btnSubmit = dialog.findViewById(R.id.btnSubmit);
        btnCancel = dialog.findViewById(R.id.btnCancel);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtAge.setText(String.valueOf(numberPicker.getValue()));
                dialog.dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    @OnClick(R.id.btnSubmit)
    public void updateInfo() {
        if (!edtAge.getText().toString().equals("")) {
            age = Integer.parseInt(edtAge.getText().toString());
        } else {
            Log.d(TAG, "updateInfo: Age null");
        }
        name = edtName.getText().toString();
        status = edtStatus.getText().toString();
        if (isEmailSigned()) {
            email = edtEmail.getHint().toString();
        } else {
            email = edtEmail.getText().toString();
        }
        phone = edtPhone.getText().toString();

        user.setName(name);
        user.setStatus(status);
        user.setEmail(email);
        user.setAge(age);
        user.setSex(sex);
        user.setPhoneNumber(phone);
        User.updateUser(firebaseUser.getUid(), user);
    }

    private boolean isEmailSigned() {
        return firebaseUser.getProviders().get(0).equals(AuthUI.EMAIL_PROVIDER);
    }

    @OnClick(R.id.btnReset)
    public void resetInfo() {
        if (isEmailSigned()) {
            Log.d(TAG, "resetInfo: can not edit email");
            edtEmail.setKeyListener(null);
            TooltipCompat.setTooltipText(edtEmail, emailTooltipText);
        }
        String[] allSex = getResources().getStringArray(R.array.sex);
        for (int i = 0; i < allSex.length; i++) {
            if (sex.equals(allSex[i])) {
                spinnerSex.setSelection(i);
            }
        }
        edtName.setHint(name);
        edtEmail.setHint(email);
        edtAge.setHint(String.valueOf(age));
        edtPhone.setHint(phone);
    }
}
