package com.example.myapplication.entity;

public class UserInfo {
    private String created_time;
    private String username;
    private String nickname;
    private int user_status;
    private String last_active;
    private String summary;
    private String mailbox;

    public UserInfo(String created_time, String username, String nickname, int user_status, String last_active, String summary, String mailbox) {
        this.created_time = created_time;
        this.username = username;
        this.nickname = nickname;
        this.user_status = user_status;
        this.last_active = last_active;
        this.summary = summary;
        this.mailbox = mailbox;
    }

    public String getCreated_time() {
        return created_time;
    }

    public void setCreated_time(String created_time) {
        this.created_time = created_time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getUser_status() {
        return user_status;
    }

    public void setUser_status(int user_status) {
        this.user_status = user_status;
    }

    public String getLast_active() {
        return last_active;
    }

    public void setLast_active(String last_active) {
        this.last_active = last_active;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getMailbox() {
        return mailbox;
    }

    public void setMailbox(String mailbox) {
        this.mailbox = mailbox;
    }

// getters and setters
}