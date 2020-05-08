package com.example.blogapp.model;

public class Blog {
    private String admin,title,description,image,blogId;
    private long date;
    private Object posts;

    public Blog() {
    }

    public Blog(String admin,String title, String description, String image, String blogId, long date, Object posts) {
        this.admin = admin;
        this.title = title;
        this.description = description;
        this.image = image;
        this.blogId = blogId;
        this.date = date;
        this.posts = posts;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBlogId() {
        return blogId;
    }

    public void setBlogId(String blogId) {
        this.blogId = blogId;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public Object getPosts() {
        return posts;
    }

    public void setPosts(Object posts) {
        this.posts = posts;
    }
}
