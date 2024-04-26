package com.example.sukappusers.UserSection;

public class Notification {
    private String notificationKey;
    private String title;
    private String body;
    private String imageUrl;
    private String date;
    private String time;
    private String category;
    private String status;

    public Notification() {
        // Default constructor required for Firebase
    }


    public Notification(String notificationKey, String title, String body, String imageUrl, String date, String time, String category, String status) {
        this.notificationKey = notificationKey;
        this.title = title;
        this.body = body;
        this.imageUrl = imageUrl;
        this.date = date;
        this.time = time;
        this.category = category;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotificationKey() {
        return notificationKey;
    }

    public void setNotificationKey(String notificationKey) {
        this.notificationKey = notificationKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
