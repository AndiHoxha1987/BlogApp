package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.blogapp.adapter.BlogAdapter;
import com.example.blogapp.model.Blog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class FindBlogActivity extends AppCompatActivity {

    private RecyclerView findBlogRecyclerList;
    private DatabaseReference BlogRef;
    private EditText enterEmail;
    private TextView noResult;
    private BlogAdapter blogAdapter;
    private final List<Blog> blogList = new ArrayList<>();
    private String currentUserId;
    private final List<String> contactList = new ArrayList<>();
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_blog);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null){
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        initializeFields();
        BlogRef = FirebaseDatabase.getInstance().getReference().child("Blog");

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blogList.clear();
                SearchForNonFollowBlog();
            }
        });
    }

    private void initializeFields(){
        blogAdapter = new BlogAdapter(blogList,this);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this);
        enterEmail = findViewById(R.id.search_friends_text_input);
        searchButton = findViewById(R.id.search_friends_button);
        noResult = findViewById(R.id.no_result);
        findBlogRecyclerList = findViewById(R.id.find_blog_recycler_list);
        findBlogRecyclerList.setLayoutManager(linearLayoutManager2);
        findBlogRecyclerList.setAdapter(blogAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        blogList.clear();
    }

    private void SearchForNonFollowBlog() {
            final String email = enterEmail.getText().toString();
            searchForFollowBlogList();
            BlogRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot dt : dataSnapshot.getChildren()){
                        Blog currentBlog = dt.getValue(Blog.class);
                        assert currentBlog!=null;
                        String blogName = currentBlog.getTitle();
                        String blogId = currentBlog.getBlogId();
                        boolean exist = false;
                        if(blogName.contains(email)){
                            noResult.setVisibility(View.GONE);
                            for(String st :contactList){
                                if(st.equals(blogId)){
                                    exist = true;
                                    break;
                                }
                            }
                            if(!exist){
                                blogList.add(currentBlog);
                                blogAdapter.notifyDataSetChanged();
                                findBlogRecyclerList.setAdapter(blogAdapter);
                            }
                        }else {
                            if(blogList.size()==0){
                                noResult.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            enterEmail.setText("");

    }

    private void searchForFollowBlogList(){
        //need te search first for blog which user has followed, make a list and match that list with search result
        FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot dt:dataSnapshot.getChildren()){
                            if(dt.hasChild("Contact Status")){
                                String st = dt.child("Contact Status").getValue(String.class);
                                assert st != null;
                                if(st.equals("Blog")){
                                    String key = dt.getKey();
                                    contactList.add(key);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
