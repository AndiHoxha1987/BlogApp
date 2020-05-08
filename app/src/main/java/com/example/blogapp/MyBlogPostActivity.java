package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.blogapp.model.PostBlog;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import static com.example.blogapp.MainActivity.INTENT_EXTRA_BLOG_ID;

public class MyBlogPostActivity extends AppCompatActivity {

    private String blogId, currentUserId;
    private EditText title,description;
    private ImageView postPhoto;
    private static final int BlogGalleryPick = 20;
    private ProgressDialog loadingBar;
    private StorageReference BlogProfileImagesRef;
    private String downloadUrl;
    private Button createPost;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_blog_post);
        FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            currentUserId = mFirebaseUser.getUid();
        }
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeFields();

        BlogProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        postPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, BlogGalleryPick);
            }
        });
        createPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPost();
            }
        });
    }

    private void initializeFields(){
        loadingBar = new ProgressDialog(this);
        blogId = getIntent().getStringExtra(INTENT_EXTRA_BLOG_ID);
        title = findViewById(R.id.set_post_name);
        description = findViewById(R.id.set_post_description);
        createPost = findViewById(R.id.create_post);
        postPhoto = findViewById(R.id.set_post_photo);
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
                            Toast.makeText(MyBlogPostActivity.this,
                                    "Profile Image uploaded Successfully...", Toast.LENGTH_SHORT).show();

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
                                        downloadUrl = downloadUri.toString();
                                        Picasso.get().load(downloadUrl).placeholder(R.drawable.profile_image).into(postPhoto);
                                        loadingBar.dismiss();
                                    } else {
                                        Toast.makeText(MyBlogPostActivity.this,
                                                "Error: Cant download URL ", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                        } else {
                            assert task.getException() != null;
                            String message = task.getException().toString();
                            Toast.makeText(MyBlogPostActivity.this, "Error: " + message,
                                    Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }
        }
    }

    private void createPost(){
        final String setPostName = title.getText().toString();
        final String setPostDescription = description.getText().toString();
        if (TextUtils.isEmpty(setPostName)|| TextUtils.isEmpty(setPostDescription)) {
            Toast.makeText(this, "Please fill all fields....",
                    Toast.LENGTH_SHORT).show();
        }else {
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle("On Progress...");
            dialog.show();
            String key = FirebaseDatabase.getInstance().getReference().child("Blog")
                    .child(blogId).child("posts").push().getKey();
            long dt = System.currentTimeMillis();
            PostBlog newPost = new PostBlog(setPostName,setPostDescription,downloadUrl,key,blogId,currentUserId,dt);
            assert key != null;
            FirebaseDatabase.getInstance().getReference().child("Blog")
                    .child(blogId).child("posts").child(key).setValue(newPost).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        dialog.dismiss();
                        Toast.makeText(MyBlogPostActivity.this,
                                "Article Created Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MyBlogPostActivity.this, BlogAdminActivity.class);
                        intent.putExtra(INTENT_EXTRA_BLOG_ID,blogId);
                        startActivity(intent);

                    } else {
                        dialog.dismiss();
                        Toast.makeText(MyBlogPostActivity.this,
                                "Error occurred", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
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
