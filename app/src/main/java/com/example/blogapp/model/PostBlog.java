package com.example.blogapp.model;

public class PostBlog {
    private String article,content,image,postId,currentBlogId,userId;
    private long time;

    public PostBlog() {
    }

    public PostBlog(String article, String content, String image, String postId,String currentBlogId,String userId,long time) {
        this.article = article;
        this.content = content;
        this.image = image;
        this.postId = postId;
        this.currentBlogId = currentBlogId;
        this.userId = userId;
        this.time = time;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPostId() {
        return postId;
    }

    public String getCurrentBlogId() {
        return currentBlogId;
    }

    public void setCurrentBlogId(String currentBlogId) {
        this.currentBlogId = currentBlogId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
