package com.example.sukappusers.UserSection;


public class Faculty {
    String facultyId, facultyName, facultyDescription, facultyRole, facultyContact, imageUrl;

    public Faculty() {
    }

    public Faculty(String facultyId, String facultyName, String facultyDescription, String facultyRole, String facultyContact, String imageUrl) {
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.facultyDescription = facultyDescription;
        this.facultyRole = facultyRole;
        this.facultyContact = facultyContact;
        this.imageUrl = imageUrl;
    }

    public String getFacultyRole() {
        return facultyRole;
    }

    public void setFacultyRole(String facultyRole) {
        this.facultyRole = facultyRole;
    }

    public String getFacultyContact() {
        return facultyContact;
    }

    public void setFacultyContact(String facultyContact) {
        this.facultyContact = facultyContact;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getFacultyDescription() {
        return facultyDescription;
    }

    public void setFacultyDescription(String facultyDescription) {
        this.facultyDescription = facultyDescription;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

