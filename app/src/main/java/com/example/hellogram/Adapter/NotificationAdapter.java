package com.example.hellogram.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hellogram.Fragments.PostDetailFragment;
import com.example.hellogram.Fragments.ProfileFragment;
import com.example.hellogram.Model.Notification;
import com.example.hellogram.Model.Post;
import com.example.hellogram.Model.User;
import com.example.hellogram.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>{

    private final Context mContext;
    private final List<Notification> mNotifications;

    public NotificationAdapter(Context mContext, List<Notification> mNotifications) {
        this.mContext = mContext;
        this.mNotifications = mNotifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = mNotifications.get(position);

        getUser(holder.imageProfile, holder.username, notification.getUserid());
        holder.comment.setText(notification.getText());

        if (notification.isPost()){
            holder.postImage.setVisibility(View.VISIBLE);
            getPostImage(holder.postImage, notification.getPostid());
        }
        else {
            holder.postImage.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (notification.isPost()){
                mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
                        .edit().putString("postId", notification.getPostid()).apply();

                ((FragmentActivity)mContext).getSupportFragmentManager()
                        .beginTransaction().replace(R.id.fragment_container, new PostDetailFragment()).commit();
            }
            else{
                mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                        .edit().putString("profileId", notification.getUserid()).apply();

                ((FragmentActivity)mContext).getSupportFragmentManager()
                        .beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mNotifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView imageProfile;
        public ImageView postImage;
        public TextView username;
        public TextView comment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProfile = itemView.findViewById(R.id.image_profile);
            postImage = itemView.findViewById(R.id.post_image);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
        }
    }

    private void getPostImage(ImageView postImage, String postId) {
        FirebaseDatabase.getInstance().getReference().child("Posts").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                assert post != null;
                Picasso.get().load(post.getImageurl()).placeholder(R.mipmap.ic_launcher).into(postImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getUser(ImageView imageView, TextView textView, String userId) {
        if (userId != null){
            FirebaseDatabase.getInstance().getReference().child("Users").child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    assert user != null;
                    if (user.getImageurl().equals("default")){
                        imageView.setImageResource(R.mipmap.ic_launcher);
                    }
                    else
                        Picasso.get().load(user.getImageurl()).into(imageView);
                    textView.setText(user.getUsername());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}
