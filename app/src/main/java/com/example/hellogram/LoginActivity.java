package com.example.hellogram;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button login;
    private TextView signupuser;

    private FirebaseAuth mAuth;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = findViewById(R.id.login);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signupuser = findViewById(R.id.signup_user);

        mAuth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);

        signupuser.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignupActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)));

        login.setOnClickListener(v -> {
            String textEmail = email.getText().toString();
            String textPassword = password.getText().toString();

            if (TextUtils.isEmpty(textEmail) || TextUtils.isEmpty(textPassword))
                Toast.makeText(LoginActivity.this, "Please fill all the Fields!", Toast.LENGTH_SHORT).show();

            else {
                loginUser(textEmail, textPassword);
            }
        });

    }
    private void loginUser(String email, String password) {
        pd.setMessage("Please Wait!");
        pd.show();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                pd.dismiss();
                Toast.makeText(LoginActivity.this, "Update the profile for " +
                        "better experience!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }

        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
