package com.example.sukappusers.UserSection;

import java.io.Serializable;

public class Complaint implements Serializable {
    private String complaintKey;
    private String subject;
    private String description;
    private String fullName;
    private String usn;
    private String batchYear;
    private String date;
    private String time;
    private String status;
    private String userId;
    private String referenceNumber;
    private String response;
    private String updatedDate;
    private String updatedTime;

    public Complaint() {
        // Empty constructor needed for Firebase
    }

    public Complaint(String complaintKey, String subject, String description, String fullName, String usn, String batchYear, String date, String time, String status, String userId, String referenceNumber) {
        this.complaintKey = complaintKey;
        this.subject = subject;
        this.description = description;
        this.fullName = fullName;
        this.usn = usn;
        this.batchYear = batchYear;
        this.date = date;
        this.time = time;
        this.status = status;
        this.userId = userId;
        this.referenceNumber = referenceNumber;
    }

    public Complaint(String complaintKey, String subject, String description, String fullName, String usn, String batchYear, String date, String time, String status, String userId, String referenceNumber, String response, String updatedDate, String updatedTime) {
        this.complaintKey = complaintKey;
        this.subject = subject;
        this.description = description;
        this.fullName = fullName;
        this.usn = usn;
        this.batchYear = batchYear;
        this.date = date;
        this.time = time;
        this.status = status;
        this.userId = userId;
        this.referenceNumber = referenceNumber;
        this.response = response;
        this.updatedDate = updatedDate;
        this.updatedTime = updatedTime;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getComplaintKey() {
        return complaintKey;
    }

    public void setComplaintKey(String complaintKey) {
        this.complaintKey = complaintKey;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsn() {
        return usn;
    }

    public void setUsn(String usn) {
        this.usn = usn;
    }

    public String getBatchYear() {
        return batchYear;
    }

    public void setBatchYear(String batchYear) {
        this.batchYear = batchYear;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
