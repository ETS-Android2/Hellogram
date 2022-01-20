package com.example.hellogram;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.hendraanggrian.appcompat.socialview.Hashtag;
import com.hendraanggrian.appcompat.widget.HashtagArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PostActivity extends AppCompatActivity {

    private Uri imageUri;
    private String imageUrl;

    private ImageView close;
    private ImageView imageAdded;
    private TextView post;
    SocialAutoCompleteTextView description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        close = findViewById(R.id.close);
        imageAdded = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);

        close.setOnClickListener(v -> {
            startActivity(new Intent(PostActivity.this, MainActivity.class));
            finish();
        });

        post.setOnClickListener(v -> upload());

        CropImage.activity().start(PostActivity.this);

    }

    private void upload() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading...");
        pd.show();

        if (imageUri != null){
            StorageReference filePath = FirebaseStorage.getInstance().getReference("Posts").child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            StorageTask uploadedTask = filePath.putFile(imageUri);

            uploadedTask.continueWithTask(task -> {
                if (!task.isSuccessful()) throw task.getException();

                return filePath.getDownloadUrl();

            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                Uri downloadUri = task.getResult();
                imageUrl = downloadUri.toString();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                String postId = ref.push().getKey();

                HashMap<String, Object> map = new HashMap<>();

                map.put("postid", postId);
                map.put("imageurl", imageUrl);
                map.put("description", description.getText().toString());
                map.put("publisher", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

                assert postId != null;
                ref.child(postId).setValue(map);

                DatabaseReference mHashTagRef = FirebaseDatabase.getInstance().getReference().child("HashTags");
                List<String> hashTags = description.getHashtags();
                if (!hashTags.isEmpty()){
                    for (String tag : hashTags){
                        map.clear();

                        map.put("tag", tag.toLowerCase());
                        map.put("postid", postId);

                        mHashTagRef.child(tag.toLowerCase()).child(postId).setValue(map);
                    }
                }

                pd.dismiss();
                startActivity(new Intent(PostActivity.this, MainActivity.class));
                finish();
            }).addOnFailureListener(e -> Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
        }
        else {
            Toast.makeText(this, "No image was selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            assert result != null;
            imageUri = result.getUri();

            imageAdded.setImageURI(imageUri);
        }

        else{
            Toast.makeText(this, "Try Again!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PostActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        ArrayAdapter<Hashtag> hashtagAdapter = new HashtagArrayAdapter<>(getApplicationContext());

        FirebaseDatabase.getInstance().getReference().child("HashTags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot eachSnapShot: snapshot.getChildren()){
                    hashtagAdapter.add(new Hashtag(eachSnapShot.getKey(), (int) eachSnapShot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        description.setHashtagAdapter(hashtagAdapter);
    }
}