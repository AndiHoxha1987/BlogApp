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
import android.widget.TextView;
import android.widget.Toast;
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
import static com.example.blogapp.MainActivity.INTENT_EXTRA_BLOG_ID;
import static com.example.blogapp.MainActivity.INTENT_EXTRA_POST_ID;
import static com.example.blogapp.MainActivity.INTENT_EXTRA_USER_ID;

public class ReadBlogActivity extends AppCompatActivity {
    private String currentUserId,blogId,postId,userId;
    private TextView title, description;
    private EditText editTitle,editDescription;
    private Button editButton,goToBlogProfile;
    private ImageView image;
    private ValueEventListener mListener;
    private Boolean editIsActive = false;
    private static final int BlogGalleryPick = 21;
    private ProgressDialog loadingBar;
    private StorageReference BlogProfileImagesRef;
    private String downloadUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_blog);

        FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            currentUserId = mFirebaseUser.getUid();
        }
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeFields();

        BlogProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        goToBlogProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReadBlogActivity.this, BlogAdminActivity.class);
                intent.putExtra(INTENT_EXTRA_BLOG_ID, blogId);
                startActivity(intent);
            }
        });

        mListener = FirebaseDatabase.getInstance().getReference().child("Blog")
                .child(blogId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            if(dataSnapshot.hasChild("posts")){
                                if(dataSnapshot.child("posts").hasChild(postId)){

                                    String article =dataSnapshot.child("posts").child(postId).child("article").getValue(String.class);
                                    String content =dataSnapshot.child("posts").child(postId).child("content").getValue(String.class);
                                    title.setText(article);
                                    description.setText(content);
                                    editTitle.setText(article);
                                    editDescription.setText(content);
                                    if(dataSnapshot.child("posts").child(postId).hasChild("image")){
                                        String blogImage = dataSnapshot.child("posts").child(postId).child("image").getValue(String.class);
                                        Picasso.get().load(blogImage).into(image);

                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if(currentUserId.equals(userId)){
            editButton.setVisibility(View.VISIBLE);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent, BlogGalleryPick);
                }
            });
        }

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editIsActive){
                    editIsActive = true;
                    title.setVisibility(View.GONE);
                    description.setVisibility(View.GONE);
                    editTitle.setVisibility(View.VISIBLE);
                    editDescription.setVisibility(View.VISIBLE);
                    editButton.setText(R.string.save);
                }
                else{
                    updatePost();
                    title.setVisibility(View.VISIBLE);
                    description.setVisibility(View.VISIBLE);
                    editTitle.setVisibility(View.GONE);
                    editDescription.setVisibility(View.GONE);
                    editButton.setText(R.string.edit);
                    editIsActive = false;
                }
            }
        });
    }

    private void initializeFields(){
        loadingBar = new ProgressDialog(this);
        userId = getIntent().getStringExtra(INTENT_EXTRA_USER_ID);
        blogId = getIntent().getStringExtra(INTENT_EXTRA_BLOG_ID);
        postId = getIntent().getStringExtra(INTENT_EXTRA_POST_ID);
        title = findViewById(R.id.post_title);
        description = findViewById(R.id.post_description);
        image = findViewById(R.id.post_photo);
        editButton = findViewById(R.id.edit_read_blog);
        goToBlogProfile = findViewById(R.id.go_to_blog_profile);
        editTitle = findViewById(R.id.edit_post_title);
        editDescription = findViewById(R.id.edit_post_description);
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
                            Toast.makeText(ReadBlogActivity.this,
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
                                        Picasso.get().load(downloadUrl).into(image);
                                        loadingBar.dismiss();
                                    } else {
                                        Toast.makeText(ReadBlogActivity.this,
                                                "Error: Cant download URL ", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                        } else {
                            assert task.getException() != null;
                            String message = task.getException().toString();
                            Toast.makeText(ReadBlogActivity.this, "Error: " + message,
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
        if(mListener != null){
            FirebaseDatabase.getInstance().getReference().child("Blog")
                    .child(blogId).removeEventListener(mListener);
        }
    }

    private void updatePost(){
        final String setPostName = editTitle.getText().toString();
        final String setPostDescription = editDescription.getText().toString();
        if (TextUtils.isEmpty(setPostName)|| TextUtils.isEmpty(setPostDescription)) {
            Toast.makeText(this, "Please fill all fields....",
                    Toast.LENGTH_SHORT).show();
        }
        else {
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle("On Progress...");
            dialog.show();
            long dt = System.currentTimeMillis();
            PostBlog newPost = new PostBlog(setPostName,setPostDescription,downloadUrl,postId,blogId,currentUserId,dt);
            FirebaseDatabase.getInstance().getReference().child("Blog")
                    .child(blogId).child("posts").child(postId).setValue(newPost).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        dialog.dismiss();
                        Toast.makeText(ReadBlogActivity.this,
                                "Article Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        dialog.dismiss();
                        Toast.makeText(ReadBlogActivity.this,
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
