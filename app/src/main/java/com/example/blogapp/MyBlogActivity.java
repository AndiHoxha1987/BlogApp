package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.example.blogapp.adapter.BlogAdminAdapter;
import com.example.blogapp.model.Blog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;

public class MyBlogActivity extends AppCompatActivity {
    private RecyclerView myBlogList;
    private BlogAdminAdapter myBlogAdapter;
    private final List<Blog> blogArrayList = new ArrayList<>();
    private String currentUserId;
    private ChildEventListener mListener;
    private TextView createNewBlog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_blog);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeFields();

        createNewBlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyBlogActivity.this, CreateNewBlogActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        blogArrayList.clear();
        mListener = FirebaseDatabase.getInstance().getReference().child("Blog").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Blog blog = dataSnapshot.getValue(Blog.class);
                assert blog != null;
                String admin = blog.getAdmin();
                if(currentUserId.equals(admin)){
                    blogArrayList.add(blog);
                    myBlogAdapter.notifyDataSetChanged();
                    myBlogList.setAdapter(myBlogAdapter);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mListener!=null){
            FirebaseDatabase.getInstance().getReference().child("Blog").removeEventListener(mListener);
        }
    }

    private void initializeFields(){
        createNewBlog = findViewById(R.id.go_to_create_new_blog);
        myBlogList = findViewById(R.id.my_blog_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        myBlogList.setLayoutManager(linearLayoutManager);
        myBlogAdapter = new BlogAdminAdapter(blogArrayList,this);
        myBlogList.setAdapter(myBlogAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(MyBlogActivity.this, MainActivity.class);
            startActivity(intent);
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
