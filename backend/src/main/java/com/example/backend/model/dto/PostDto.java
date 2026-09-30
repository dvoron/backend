package com.example.backend.model.dto;

import java.util.List;

public class PostDto {
    private Long id;
    private String author;
    private String title;
    private String content;
    private String timestamp;
    private List<CommentDto> comments;

    private boolean showComments = false;
    private String newCommentContent = "";

    public PostDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<CommentDto> getComments() {
        return comments;
    }

    public void setComments(List<CommentDto> comments) {
        this.comments = comments;
    }

    public boolean isShowComments() {
        return showComments;
    }

    public void setShowComments(boolean showComments) {
        this.showComments = showComments;
    }

    public String getNewCommentContent() {
        return newCommentContent;
    }

    public void setNewCommentContent(String newCommentContent) {
        this.newCommentContent = newCommentContent;
    }
}
