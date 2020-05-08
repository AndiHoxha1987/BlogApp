package com.example.blogapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blogapp.R;
import com.example.blogapp.ReadBlogActivity;
import com.example.blogapp.model.PostBlog;
import com.squareup.picasso.Picasso;
import java.util.List;

import static com.example.blogapp.MainActivity.INTENT_EXTRA_BLOG_ID;
import static com.example.blogapp.MainActivity.INTENT_EXTRA_POST_ID;
import static com.example.blogapp.MainActivity.INTENT_EXTRA_USER_ID;

public class BlogPostAdapter extends RecyclerView.Adapter<BlogPostAdapter.BlogViewHolder> {


    private final List<PostBlog> userBlogList;
    private final Context context;


    public BlogPostAdapter(List<PostBlog> userBlogList,Context context) {
        this.userBlogList = userBlogList;
        this.context = context;
    }

    @SuppressWarnings("WeakerAccess")
    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        public final TextView description;
        public final TextView title;
        public final ImageView profile;

        @SuppressWarnings("WeakerAccess")
        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);

            description = itemView.findViewById(R.id.blog_description_model);
            title = itemView.findViewById(R.id.blog_title_model);
            profile = itemView.findViewById(R.id.blog_image_model);
        }
    }

    @NonNull
    @Override
    public BlogPostAdapter.BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.blog_model, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final BlogPostAdapter.BlogViewHolder holder, int position) {
        PostBlog currentBlog = userBlogList.get(position);
        final String userId = currentBlog.getUserId();
        final String blogId = currentBlog.getCurrentBlogId();
        String sTitle = currentBlog.getArticle();
        final String postId = currentBlog.getPostId();
        String sDescription = currentBlog.getContent();
        String sImage = currentBlog.getImage();
        holder.title.setText(sTitle);
        holder.description.setText(sDescription);
        Picasso.get().load(sImage).placeholder(R.drawable.profile_image).fit().into(holder.profile);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ReadBlogActivity.class);
                intent.putExtra(INTENT_EXTRA_USER_ID,userId);
                intent.putExtra(INTENT_EXTRA_BLOG_ID, blogId);
                intent.putExtra(INTENT_EXTRA_POST_ID, postId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userBlogList.size();
    }
}
