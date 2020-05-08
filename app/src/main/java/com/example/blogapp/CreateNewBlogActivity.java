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
import com.example.blogapp.model.Blog;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

public class CreateNewBlogActivity extends AppCompatActivity {
    private String currentUserID, blogKey;
    private DatabaseReference RootRef;
    private EditText blogName, blogDescription;
    private ImageView setBlogImage;
    private static final int BlogGalleryPick = 11;
    private ProgressDialog loadingBar;
    private StorageReference BlogProfileImagesRef;
    private String downloadUrl;
    private Button createBlogButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_blog);
        FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            currentUserID = mFirebaseUser.getUid();
        }
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeFields();

        BlogProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        RootRef = FirebaseDatabase.getInstance().getReference();

        createBlogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBlog();
            }
        });

        setBlogImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, BlogGalleryPick);
            }
        });
    }

    private void createBlog(){
        final String setBlogName = blogName.getText().toString();
        final String setBlogStatus = blogDescription.getText().toString();

        if (TextUtils.isEmpty(setBlogName)|| TextUtils.isEmpty(setBlogStatus)) {
            Toast.makeText(this, "Please write your blog name and description first....",
                    Toast.LENGTH_SHORT).show();
        }else {
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle("On Progress...");
            dialog.show();
            RootRef.child("BlogNames").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        boolean nameExist = false;
                        for(DataSnapshot data :dataSnapshot.getChildren()){
                            String name = data.child("name").getValue(String.class);
                            if(setBlogName.equals(name)){
                                nameExist = true;
                                dialog.dismiss();
                                Toast.makeText(CreateNewBlogActivity.this, "This name exist, " +
                                        "please try another blog name....", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                        if(!nameExist){
                            blogKey = RootRef.child("Blog").child(currentUserID).push().getKey();
                            long dt = System.currentTimeMillis();
                            final Blog newBlog = new Blog(currentUserID,setBlogName, setBlogStatus, null,blogKey, dt,null);
                            RootRef.child("Blog").child(blogKey).setValue(newBlog)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                dialog.dismiss();
                                                Toast.makeText(CreateNewBlogActivity.this,
                                                        "Blog Created Successfully", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(CreateNewBlogActivity.this, MyBlogActivity.class);
                                                startActivity(intent);

                                            } else {
                                                dialog.dismiss();
                                                Toast.makeText(CreateNewBlogActivity.this,
                                                        "Error occurred", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }else{
                        blogKey = RootRef.child("Blog").child(currentUserID).push().getKey();
                        long dt = System.currentTimeMillis();
                        final Blog newBlog = new Blog(currentUserID,setBlogName, setBlogStatus, downloadUrl,blogKey, dt,null);
                        RootRef.child("Blog").child(blogKey).setValue(newBlog)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            dialog.dismiss();
                                            Toast.makeText(CreateNewBlogActivity.this,
                                                    "Blog Created Successfully", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(CreateNewBlogActivity.this,MyBlogActivity.class);
                                            startActivity(intent);

                                        } else {
                                            dialog.dismiss();
                                            Toast.makeText(CreateNewBlogActivity.this,
                                                    "Error occurred", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    dialog.dismiss();
                    Toast.makeText(CreateNewBlogActivity.this,
                            "Error occurred", Toast.LENGTH_SHORT).show();
                }
            });
        }
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

                StorageReference filePath = BlogProfileImagesRef.child(profilePicName+currentUserID + ".jpg");
                //upload file in storage
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(CreateNewBlogActivity.this,
                                    "Profile Image uploaded Successfully...", Toast.LENGTH_SHORT).show();

                            //after uploading file get url for corresponding file
                            final StorageReference ref = BlogProfileImagesRef.child(profilePicName+currentUserID + ".jpg");
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
                                        Picasso.get().load(downloadUrl).placeholder(R.drawable.profile_image).into(setBlogImage);
                                        loadingBar.dismiss();
                                    } else {
                                        Toast.makeText(CreateNewBlogActivity.this,
                                                "Error: Cant download URL ", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });

                        } else {
                            assert task.getException() != null;
                            String message = task.getException().toString();
                            Toast.makeText(CreateNewBlogActivity.this, "Error: " + message,
                                    Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }
        }
    }

    private void initializeFields(){
        createBlogButton = findViewById(R.id.create_blog);
        loadingBar = new ProgressDialog(this);
        blogName = findViewById(R.id.set_blog_name);
        setBlogImage = findViewById(R.id.set_blog_cover_photo);
        Picasso.get().load(downloadUrl).placeholder(R.drawable.profile_image).into(setBlogImage);
        blogDescription = findViewById(R.id.set_blog_description);
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
