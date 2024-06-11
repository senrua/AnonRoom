package com.example.myapplication.entity;

public class ContactMsg {
    private String nickname;
    private String username;
    private String friendship_starttime;
    private int friendship_chattime;
    private String request_name;
    private String request_nickname;
    private int location;
    private int agree_status;
    private String created_time;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFriendship_starttime() {
        return friendship_starttime;
    }

    public void setFriendship_starttime(String friendship_starttime) {
        this.friendship_starttime = friendship_starttime;
    }

    public int getFriendship_chattime() {
        return friendship_chattime;
    }

    public void setFriendship_chattime(int friendship_chattime) {
        this.friendship_chattime = friendship_chattime;
    }

    public String getRequest_name() {
        return request_name;
    }

    public void setRequest_name(String request_name) {
        this.request_name = request_name;
    }

    public String getRequest_nickname() {
        return request_nickname;
    }

    public void setRequest_nickname(String request_nickname) {
        this.request_nickname = request_nickname;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public int getAgree_status() {
        return agree_status;
    }

    public void setAgree_status(int agree_status) {
        this.agree_status = agree_status;
    }

    public String getCreated_time() {
        return created_time;
    }

    public void setCreated_time(String created_time) {
        this.created_time = created_time;
    }
}
