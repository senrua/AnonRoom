package com.example.myapplication.entity;

import java.time.ZonedDateTime;

public class NewFriendMsg {
    private String requestName;
    private String requestNickname;
    private int location;
    private int agreeStatus;
    private String createdTime;

    // Getter and Setter methods
    public String getRequestName() {
        return requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    public String getRequestNickname() {
        return requestNickname;
    }

    public void setRequestNickname(String requestNickname) {
        this.requestNickname = requestNickname;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public int getAgreeStatus() {
        return agreeStatus;
    }

    public void setAgreeStatus(int agreeStatus) {
        this.agreeStatus = agreeStatus;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }
}