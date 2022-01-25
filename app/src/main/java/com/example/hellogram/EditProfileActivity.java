package com.example.hellogram;



import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hellogram.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.time.LocalDate;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView close;
    private CircleImageView imageProfile;
    private TextView save;
    private TextView changePhoto;
    private MaterialEditText fullname;
    private MaterialEditText username;
    private MaterialEditText bio;
    private DatePicker birthDate;
    private LocalDate localBirthDate;
    private TextView changePassword;

    ProgressDialog pd;


    private FirebaseUser fUser;

    private Uri mImageUri;
    private StorageTask uploadTask;
    private StorageReference storageRef;

    public EditProfileActivity() {
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        close = findViewById(R.id.close);
        imageProfile = findViewById(R.id.image_profile);
        save = findViewById(R.id.save);
        changePhoto = findViewById(R.id.change_photo);
        fullname = findViewById(R.id.fullname);
        username = findViewById(R.id.username);
        bio = findViewById(R.id.bio);
        birthDate = findViewById(R.id.date);
        changePassword = findViewById(R.id.change_password);
        localBirthDate = LocalDate.of(getBirthDate().getYear(), (getBirthDate().getMonth())+1, getBirthDate().getDayOfMonth());

        pd = new ProgressDialog(this);

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference("Uploads");

        FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                fullname.setText(user.getName());
                username.setText(user.getUsername());
                bio.setText(user.getBio());
                Picasso.get().load(user.getImageurl()).into(imageProfile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        close.setOnClickListener(v -> finish());

        changePhoto.setOnClickListener(v -> CropImage.activity().setCropShape(CropImageView.CropShape.OVAL).start(EditProfileActivity.this));

        imageProfile.setOnClickListener(v -> CropImage.activity().setCropShape(CropImageView.CropShape.OVAL).start(EditProfileActivity.this));

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("Please Wait!");
                pd.show();
                FirebaseAuth.getInstance().sendPasswordResetEmail(fUser.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(EditProfileActivity.this, "A reset password massage sent to your email!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(EditProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        pd.dismiss();
                    }
                });
            }
        });

        save.setOnClickListener(v -> {
            uploadProfile();
            finish();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void uploadProfile() {

        String year = String.valueOf(getBirthDate().getYear());
        String month = String.valueOf(getBirthDate().getMonth()+1);
        String day = String.valueOf(getBirthDate().getDayOfMonth());

        String dateOfBirth = year + "-" + month + "-" + day;

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", fullname.getText().toString());
        map.put("username", username.getText().toString());
        map.put("bio", bio.getText().toString());
        map.put("date", dateOfBirth);

        FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid()).updateChildren(map);
    }

    private void uploadImage() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading...");
        pd.show();

        if (mImageUri != null){
            StorageReference fileRef = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));

            uploadTask = fileRef.putFile(mImageUri);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) throw task.getException();

                return fileRef.getDownloadUrl();

            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                if (task.isSuccessful()){
                    Uri downloadUri = task.getResult();
                    String url = downloadUri.toString();

                    FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid()).child("imageurl").setValue(url);
                }
                else {
                    Toast.makeText(EditProfileActivity.this, "Upload failed!", Toast.LENGTH_SHORT).show();
                }
                pd.dismiss();
            });
        } else {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
            pd.dismiss();
        }
    }

    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(uri));
    }

    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();
            uploadImage();

        }
        else {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public int calculateAge() {
        if (getBirthDate() == null)
            return -1;
        return localBirthDate.until(LocalDate.now()).getYears();
    }

    public DatePicker getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(DatePicker birthDate) {
        this.birthDate = birthDate;
    }

}