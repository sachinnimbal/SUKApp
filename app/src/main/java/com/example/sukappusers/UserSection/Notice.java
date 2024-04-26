package com.example.sukappusers.UserSection;
public class Notice {
    private String noticeKey;
    private String noticeTitle;
    private String noticeDescription;
    private String noticeDate;
    private String noticeTime;
    private String imageUrl;
    private String status;

    public Notice() {
        // Default constructor required for calls to DataSnapshot.getValue(Notice.class)
    }

    public Notice(String noticeKey, String noticeTitle, String noticeDescription, String noticeDate, String noticeTime, String imageUrl, String status) {
        this.noticeKey = noticeKey;
        this.noticeTitle = noticeTitle;
        this.noticeDescription = noticeDescription;
        this.noticeDate = noticeDate;
        this.noticeTime = noticeTime;
        this.imageUrl = imageUrl;
        this.status = status;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNoticeKey() {
        return noticeKey;
    }

    public void setNoticeKey(String noticeKey) {
        this.noticeKey = noticeKey;
    }

    public String getNoticeTitle() {
        return noticeTitle;
    }

    public void setNoticeTitle(String noticeTitle) {
        this.noticeTitle = noticeTitle;
    }

    public String getNoticeDescription() {
        return noticeDescription;
    }

    public void setNoticeDescription(String noticeDescription) {
        this.noticeDescription = noticeDescription;
    }

    public String getNoticeDate() {
        return noticeDate;
    }

    public void setNoticeDate(String noticeDate) {
        this.noticeDate = noticeDate;
    }

    public String getNoticeTime() {
        return noticeTime;
    }

    public void setNoticeTime(String noticeTime) {
        this.noticeTime = noticeTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
