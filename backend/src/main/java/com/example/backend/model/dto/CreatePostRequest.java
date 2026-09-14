package com.example.backend.model.dto;

public class CreatePostRequest {
    private String title;
    private String content;

    public CreatePostRequest() {}

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
}
