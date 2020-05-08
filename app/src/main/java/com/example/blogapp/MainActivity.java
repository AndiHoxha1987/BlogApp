package com.example.blogapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blogapp.adapter.BlogPostAdapter;
import com.example.blogapp.model.PostBlog;
import com.example.blogapp.nonAuthActivities.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private RecyclerView postRecycleView;
    private final List<PostBlog> postList= new ArrayList<>();
    private BlogPostAdapter blogPostAdapter;
    private DatabaseReference BlogReference;
    private ValueEventListener mListener;
    public static final String INTENT_EXTRA_USER_ID = "com.example.blogapp.user_id";
    public static final String INTENT_EXTRA_BLOG_ID = "com.example.blogapp.blog_id";
    public static final String INTENT_EXTRA_POST_ID = "com.example.blogapp.post_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        String currentUserID;
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            currentUserID=currentUser.getUid();
            initializeFields();

            BlogReference = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);

            mListener = BlogReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        for(DataSnapshot data :dataSnapshot.getChildren()){
                            if(data.hasChild("Contact Status")){
                                String dataStatus = data.child("Contact Status").getValue(String.class);
                                assert dataStatus != null;
                                if(dataStatus.equals("Blog")){
                                    String key = data.getKey();
                                    assert key != null;
                                    FirebaseDatabase.getInstance().getReference().child("Blog")
                                            .child(key).child("posts").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                for(DataSnapshot data : dataSnapshot.getChildren()){
                                                    PostBlog currentPost = data.getValue(PostBlog.class);
                                                    postList.add(currentPost);
                                                    Collections.sort(postList,new Comparator<PostBlog>(){
                                                        @Override
                                                        public int compare(PostBlog o1, PostBlog o2) {
                                                            return o1.getPostId().compareTo(o2.getPostId());
                                                        }
                                                    });
                                                    blogPostAdapter.notifyDataSetChanged();
                                                    postRecycleView.setAdapter(blogPostAdapter);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else{
            sendUserToLoginActivity();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mListener != null){
            BlogReference.removeEventListener(mListener);
        }
    }

    private void initializeFields(){
        postRecycleView = findViewById(R.id.blog_list);
        blogPostAdapter = new BlogPostAdapter(postList,this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postRecycleView.setLayoutManager(linearLayoutManager);
        postRecycleView.setAdapter(blogPostAdapter);
        postList.clear();
    }

    private void sendUserToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_logout_option){
            mAuth.signOut();
            sendUserToLoginActivity();
        }
        if (item.getItemId() == R.id.main_find_blog_option){
            sendUserToFindBlogActivity();
        }
        if (item.getItemId() == R.id.blog_view_and_create){
            sendUserToBlogActivity();
        }
        return true;
    }

    private void sendUserToBlogActivity() {
        Intent findFriendsIntent = new Intent(MainActivity.this, MyBlogActivity.class);
        startActivity(findFriendsIntent);
    }

    private void sendUserToFindBlogActivity(){
        Intent findFriendsIntent = new Intent(MainActivity.this, FindBlogActivity.class);
        startActivity(findFriendsIntent);
    }

}