package com.example.blogapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blogapp.R;
import com.example.blogapp.model.Blog;
import com.example.blogapp.BlogAdminActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import java.util.List;

import static com.example.blogapp.MainActivity.INTENT_EXTRA_BLOG_ID;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogViewHolder> {

    private final List<Blog> userBlogList;
    private final Context context;
    private String currentUserId;

    public BlogAdapter(List<Blog> userBlogList,Context context) {
        this.userBlogList = userBlogList;
        this.context = context;
    }

    @SuppressWarnings("WeakerAccess")
    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        public final TextView description;
        public final TextView title,follow;
        public final ImageView profile;

        @SuppressWarnings("WeakerAccess")
        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);

            description = itemView.findViewById(R.id.blog_description_model);
            title = itemView.findViewById(R.id.blog_title_model);
            profile = itemView.findViewById(R.id.blog_image_model);
            follow = itemView.findViewById(R.id.blog_follow);
        }
    }

    @NonNull
    @Override
    public BlogAdapter.BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.blog_model, parent, false);
        if(FirebaseAuth.getInstance().getCurrentUser()!= null){
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final BlogAdapter.BlogViewHolder holder, int position) {
        Blog currentBlog = userBlogList.get(position);
        String admin = currentBlog.getAdmin();
        if(!currentUserId.equals(admin)){
            holder.follow.setVisibility(View.VISIBLE);
        }
        final String sTitle = currentBlog.getTitle();
        final String blogId = currentBlog.getBlogId();
        String sDescription = currentBlog.getDescription();
        String sImage = currentBlog.getImage();
        holder.title.setText(sTitle);
        holder.description.setText(sDescription);
        Picasso.get().load(sImage).placeholder(R.drawable.profile_image).into(holder.profile);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BlogAdminActivity.class);
                intent.putExtra(INTENT_EXTRA_BLOG_ID, blogId);
                context.startActivity(intent);
            }
        });
        holder.follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.follow.getText().equals("Follow")){
                    holder.follow.setText(R.string.un_follow);
                    FirebaseDatabase.getInstance().getReference().child("Contacts")
                            .child(currentUserId).child(blogId).child("Contact Status").setValue("Blog");
                    Toast.makeText(context,"You started to follow "+sTitle,Toast.LENGTH_SHORT).show();
                }else{
                    holder.follow.setText(R.string.follow);
                    FirebaseDatabase.getInstance().getReference().child("Contacts")
                            .child(currentUserId).child(blogId).removeValue();
                    Toast.makeText(context,"You removed "+sTitle,Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userBlogList.size();
    }
}
