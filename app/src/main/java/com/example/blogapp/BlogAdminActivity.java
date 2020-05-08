package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blogapp.adapter.BlogPostAdapter;
import com.example.blogapp.model.PostBlog;
import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import java.util.ArrayList;
import java.util.List;

import static com.example.blogapp.MainActivity.INTENT_EXTRA_BLOG_ID;


public class BlogAdminActivity extends AppCompatActivity {
    private String blogId, currentUserId, blogAdmin,retImage;
    private ImageView blogCoverImage;
    private ValueEventListener mListener,bListener;
    private RecyclerView blogPost;
    private BlogPostAdapter blogPostAdapter;
    private final List<PostBlog> blogPostArrayList = new ArrayList<>();
    private TextView title, description, newPost;
    private static final int BlogGalleryPick = 10;
    private ProgressDialog loadingBar;
    private StorageReference BlogProfileImagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_admin);
        FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            currentUserId = mFirebaseUser.getUid();
        }
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeFields();

        BlogProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

    }

    private void initializeFields(){
        blogId= getIntent().getStringExtra(INTENT_EXTRA_BLOG_ID);
        loadingBar = new ProgressDialog(this);
        blogCoverImage = findViewById(R.id.blog_cover_photo);
        title = findViewById(R.id.blog_title);
        description = findViewById(R.id.blog_description);
        newPost = findViewById(R.id.create_new_post);
        blogPost = findViewById(R.id.my_blog_post);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        blogPost.setLayoutManager(linearLayoutManager);
        blogPostAdapter = new BlogPostAdapter(blogPostArrayList, this);
        blogPost.setAdapter(blogPostAdapter);
        blogPost.setNestedScrollingEnabled(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        blogPostArrayList.clear();
        bListener = FirebaseDatabase.getInstance().getReference().child("Blog").child(blogId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            blogAdmin = dataSnapshot.child("admin").getValue(String.class);
                            String sTitle = dataSnapshot.child("title").getValue(String.class);
                            String sDescription = dataSnapshot.child("description").getValue(String.class);
                            retImage= dataSnapshot.child("image").getValue(String.class);
                            Picasso.get().load(retImage).placeholder(R.drawable.profile_image).into(blogCoverImage);
                            title.setText(sTitle);
                            description.setText(sDescription);

                            if (dataSnapshot.hasChild("posts")) {
                                for (DataSnapshot data : dataSnapshot.child("posts").getChildren()) {
                                    PostBlog newPost = data.getValue(PostBlog.class);
                                    blogPostArrayList.add(newPost);
                                    blogPostAdapter.notifyDataSetChanged();
                                    blogPost.setAdapter(blogPostAdapter);
                                }
                            }
                            if(!currentUserId.equals(blogAdmin)){
                                mListener = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId)
                                        .child(blogId).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()){
                                                    newPost.setText(R.string.un_follow);
                                                    newPost.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId)
                                                                    .child(blogId).removeValue();
                                                            Toast.makeText(BlogAdminActivity.this,"Blog unfollowed successfully..",Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }else{
                                                    newPost.setText(R.string.follow);
                                                    newPost.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId)
                                                                    .child(blogId).child("Contact Status").setValue("Blog");
                                                            Toast.makeText(BlogAdminActivity.this,"Blog followed successfully..",Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                            }
                            else{
                                newPost.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(BlogAdminActivity.this,MyBlogPostActivity.class);
                                        intent.putExtra(INTENT_EXTRA_BLOG_ID,blogId);
                                        startActivity(intent);
                                    }
                                });

                                blogCoverImage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent galleryIntent = new Intent();
                                        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                                        galleryIntent.setType("image/*");
                                        startActivityForResult(galleryIntent, BlogGalleryPick);
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BlogGalleryPick && resultCode == RESULT_OK && data != null) {
            Uri ImageUri = data.getData();
            CropImage.activity(ImageUri)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait, your profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                assert result != null;
                final Uri resultUri = result.getUri();
                long time = System.currentTimeMillis();
                final String profilePicName =  String.valueOf(time);

                StorageReference filePath = BlogProfileImagesRef.child(profilePicName+currentUserId + ".jpg");
                //upload file in storage
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(BlogAdminActivity.this,
                                    "Profile Image uploaded Successfully...", Toast.LENGTH_SHORT).show();
                            blogPostArrayList.clear();
                            //after uploading file get url for corresponding file
                            final StorageReference ref = BlogProfileImagesRef.child(profilePicName+currentUserId + ".jpg");
                            task = ref.putFile(resultUri);

                            task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }

                                    // Continue with the task to get the download URL
                                    return ref.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        final Uri downloadUri = task.getResult();
                                        assert downloadUri != null;
                                        final String downloadUrl = downloadUri.toString();
                                        FirebaseDatabase.getInstance().getReference().child("Blog").child(blogId).child("image")
                                                .setValue(downloadUrl)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            retImage = downloadUrl;
                                                            Toast.makeText(BlogAdminActivity.this,
                                                                    "Image Set Successfully...",
                                                                    Toast.LENGTH_SHORT).show();
                                                            loadingBar.dismiss();

                                                        } else {
                                                            assert task.getException() != null;
                                                            String message = task.getException().toString();
                                                            Toast.makeText(BlogAdminActivity.this,
                                                                    "Error: " + message, Toast.LENGTH_SHORT).show();
                                                            loadingBar.dismiss();
                                                        }
                                                    }
                                                });

                                    } else {
                                        Toast.makeText(BlogAdminActivity.this,
                                                "Error: Cant download URL ", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });

                        } else {
                            assert task.getException() != null;
                            String message = task.getException().toString();
                            Toast.makeText(BlogAdminActivity.this, "Error: " + message,
                                    Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mListener!= null){
            FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId)
                    .child(blogId).removeEventListener(mListener);
        }
        if(bListener != null){
            FirebaseDatabase.getInstance().getReference().child("Blog").child(blogId).removeEventListener(bListener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            if(currentUserId.equals(blogAdmin)){
                Intent intent = new Intent(BlogAdminActivity.this, MyBlogActivity.class);
                startActivity(intent);
                this.finish();
                return true;
            }else{
                Intent intent = new Intent(BlogAdminActivity.this, MainActivity.class);
                startActivity(intent);
                this.finish();
                return true;
            }

        }
        return super.onOptionsItemSelected(item);
    }
}
