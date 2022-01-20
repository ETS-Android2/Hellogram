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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    private EditText username;
    private EditText name;
    private EditText email;
    private EditText password;
    private Button signup;
    private TextView loginUser;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        username = findViewById(R.id.username);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signup = findViewById(R.id.signup);
        loginUser = findViewById(R.id.login_user);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);

        loginUser.setOnClickListener(v -> startActivity(new Intent(SignupActivity.this, LoginActivity.class)));

        signup.setOnClickListener(v -> {
            String textUsername = username.getText().toString();
            String textName = name.getText().toString();
            String textEmail = email.getText().toString();
            String textPassword = password.getText().toString();

            if (TextUtils.isEmpty(textUsername) ||
                    TextUtils.isEmpty(textName) ||
                    TextUtils.isEmpty(textEmail) ||
                    TextUtils.isEmpty(textPassword))
                Toast.makeText(SignupActivity.this, "You must fill all the Fields!", Toast.LENGTH_SHORT).show();

            else if (textPassword.length() < 6)
                Toast.makeText(SignupActivity.this, "Password too short!", Toast.LENGTH_SHORT).show();

            else{
                signupUser(textUsername, textName, textEmail, textPassword);
            }

        });

    }

    private void signupUser(String username, String name, String email, String password) {

        pd.setMessage("Please Wait!");
        pd.show();

        mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {

            HashMap<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("email", email);
            map.put("username", username);
            map.put("id", Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
            map.put("bio", "");
            map.put("imageurl", "default");

            mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).setValue(map).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    pd.dismiss();
                    Toast.makeText(SignupActivity.this, "Update the profile for " +
                            "better experience!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            });
        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(SignupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
