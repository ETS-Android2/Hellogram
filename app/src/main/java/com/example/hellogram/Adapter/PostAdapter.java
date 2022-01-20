package com.example.hellogram.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hellogram.CommentActivity;
import com.example.hellogram.FollowersActivity;
import com.example.hellogram.Fragments.PostDetailFragment;
import com.example.hellogram.Fragments.ProfileFragment;
import com.example.hellogram.Model.Post;
import com.example.hellogram.Model.User;
import com.example.hellogram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{

    private final Context mContext;
    private final List<Post> mPosts;

    private final FirebaseUser firebaseUser;

    public PostAdapter(Context mContext, List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false)
;       return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Post post = mPosts.get(position);
        Picasso.get().load(post.getImageurl()).into(holder.postImage);

        holder.description.setText(post.getDescription());

        FirebaseDatabase.getInstance().getReference().child("Users").child(post.getPublisher()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                assert user != null;
                if (user.getImageurl().equals("default"))
                    holder.imageProfile.setImageResource(R.mipmap.ic_launcher);


                else {
                    Picasso.get().load(user.getImageurl()).into(holder.imageProfile);
                }

                holder.username.setText(user.getUsername());
                holder.author.setText(user.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        isLiked(post.getPostid(), holder.like);
        noOfLikes(post.getPostid(), holder.noOfLikes);
        getComments(post.getPostid(), holder.noOfComments);
        isSaved(post.getPostid(), holder.save);

        holder.like.setOnClickListener(v -> {
            if (holder.like.getTag().equals("like")) {
                FirebaseDatabase.getInstance().getReference().child("Likes")
                        .child(post.getPostid()).child(firebaseUser.getUid()).setValue(true);

                addNotification(post.getPostid(), post.getPublisher());

            }
            else{
                FirebaseDatabase.getInstance().getReference().child("Likes")
                        .child(post.getPostid()).child(firebaseUser.getUid()).removeValue();
            }
        });

        holder.comment.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, CommentActivity.class);
            intent.putExtra("postId", post.getPostid());
            intent.putExtra("authorId", post.getPublisher());
            mContext.startActivity(intent);
        });

        holder.noOfComments.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, CommentActivity.class);
            intent.putExtra("postId", post.getPostid());
            intent.putExtra("authorId", post.getPublisher());
            mContext.startActivity(intent);
        });

        holder.save.setOnClickListener(v -> {
            if (holder.save.getTag().equals("save")){
                FirebaseDatabase.getInstance().getReference().child("Saves")
                        .child(firebaseUser.getUid()).child(post.getPostid()).setValue(true);

            }else{
                FirebaseDatabase.getInstance().getReference().child("Saves")
                        .child(firebaseUser.getUid()).child(post.getPostid()).removeValue();
            }
        });

        holder.imageProfile.setOnClickListener(v -> {
            mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                    .edit().putString("profileId", post.getPublisher()).apply();

            ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container,new ProfileFragment()).commit();
        });

        holder.username.setOnClickListener(v -> {
            mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                    .edit().putString("profileId", post.getPublisher()).apply();

            ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container,new ProfileFragment()).commit();
        });

        holder.author.setOnClickListener(v -> {
            mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                    .edit().putString("profileId", post.getPublisher()).apply();

            ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container,new ProfileFragment()).commit();
        });

        holder.postImage.setOnClickListener(v -> {
            mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit().putString("postId", post.getPostid()).apply();

            ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PostDetailFragment()).commit();
        });

        holder.noOfLikes.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, FollowersActivity.class);
            intent.putExtra("id", post.getPublisher());
            intent.putExtra("title", "likes");
            mContext.startActivity(intent);

        });

    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView imageProfile;
        public ImageView postImage;
        public ImageView like;
        public ImageView comment;
        public ImageView save;
        public ImageView more;

        public TextView username;
        public TextView noOfLikes;
        public TextView author;
        public TextView noOfComments;
        SocialTextView description;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProfile = itemView.findViewById(R.id.image_profile);
            postImage = itemView.findViewById(R.id.post_image);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            save = itemView.findViewById(R.id.save);
            more = itemView.findViewById(R.id.more);

            username = itemView.findViewById(R.id.username);
            noOfLikes = itemView.findViewById(R.id.no_of_likes);
            author = itemView.findViewById(R.id.author);
            noOfComments = itemView.findViewById(R.id.no_of_comments);
            description = itemView.findViewById(R.id.description);


        }
    }

    private void isLiked(String postId, ImageView imageView){
        FirebaseDatabase.getInstance().getReference().child("Likes").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                }
                else {
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void noOfLikes(String postId, TextView text){
        FirebaseDatabase.getInstance().getReference().child("Likes").child(postId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                text.setText(snapshot.getChildrenCount() + " likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getComments(String postId, TextView text){
        FirebaseDatabase.getInstance().getReference().child("Comments").child(postId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                text.setText("View All " + snapshot.getChildrenCount() + " Comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addNotification(String postid, String publisher) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userid", publisher);
        map.put("text", "Likes your post.");
        map.put("postid", postid);
        map.put("isPost", true);

        FirebaseDatabase.getInstance().getReference().child("Notifications").child(firebaseUser.getUid()).push().setValue(map);
    }

    private void isSaved(String postid, ImageView image) {
        FirebaseDatabase.getInstance().getReference().child("Saves").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postid).exists()){
                    image.setImageResource(R.drawable.ic_save_black);
                    image.setTag("saved");
                }
                else{
                    image.setImageResource(R.drawable.ic_save);
                    image.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
