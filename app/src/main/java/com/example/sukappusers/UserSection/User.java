package com.example.sukappusers.UserSection;

public class User {
    private String userId;
    private String fullName;
    private String email;
    private String profileImage;
    private boolean enabled;
    private String status;
    private String usn;
    private String parentName;
    private String address;
    private String phoneNumber;
    private String parentPhone;
    private String batchYear;
    private String registrationDate;
    private String registrationTime;
    private boolean blocked;
    private String deviceId;
    private String deviceModel;
    private String ipAddress;

    public User() {
    }


    public User(String userId, String fullName, String email, String profileImage, boolean enabled, String status, String usn, String parentName, String address, String phoneNumber, String parentPhone, String batchYear, String registrationDate, String registrationTime, boolean blocked, String deviceId, String deviceModel, String ipAddress) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.profileImage = profileImage;
        this.enabled = enabled;
        this.status = status;
        this.usn = usn;
        this.parentName = parentName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.parentPhone = parentPhone;
        this.batchYear = batchYear;
        this.registrationDate = registrationDate;
        this.registrationTime = registrationTime;
        this.blocked = blocked;
        this.deviceId = deviceId;
        this.deviceModel = deviceModel;
        this.ipAddress = ipAddress;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(String registrationTime) {
        this.registrationTime = registrationTime;
    }

    public String getBatchYear() {
        return batchYear;
    }

    public void setBatchYear(String batchYear) {
        this.batchYear = batchYear;
    }

    public String getUsn() {
        return usn;
    }

    public void setUsn(String usn) {
        this.usn = usn;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getParentPhone() {
        return parentPhone;
    }

    public void setParentPhone(String parentPhone) {
        this.parentPhone = parentPhone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

