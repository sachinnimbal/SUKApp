package com.example.sukappusers.UserSection;

public class ContactDetails {

    String collegeTitle, deptName, address, phoneNo1, phoneNo2, phoneNo3, phoneNo4, imageURL;

    public ContactDetails(String collegeTitle, String deptName, String address,
                          String phoneNo1, String phoneNo2, String phoneNo3, String phoneNo4, String imageURL) {
        this.collegeTitle = collegeTitle;
        this.deptName = deptName;
        this.address = address;
        this.phoneNo1 = phoneNo1;
        this.phoneNo2 = phoneNo2;
        this.phoneNo3 = phoneNo3;
        this.phoneNo4 = phoneNo4;
        this.imageURL = imageURL;
    }

    public ContactDetails() {
        // Required empty constructor for Firebase serialization
    }

    public String getCollegeTitle() {
        return collegeTitle;
    }

    public void setCollegeTitle(String collegeTitle) {
        this.collegeTitle = collegeTitle;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNo1() {
        return phoneNo1;
    }

    public void setPhoneNo1(String phoneNo1) {
        this.phoneNo1 = phoneNo1;
    }

    public String getPhoneNo2() {
        return phoneNo2;
    }

    public void setPhoneNo2(String phoneNo2) {
        this.phoneNo2 = phoneNo2;
    }

    public String getPhoneNo3() {
        return phoneNo3;
    }

    public void setPhoneNo3(String phoneNo3) {
        this.phoneNo3 = phoneNo3;
    }

    public String getPhoneNo4() {
        return phoneNo4;
    }

    public void setPhoneNo4(String phoneNo4) {
        this.phoneNo4 = phoneNo4;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
