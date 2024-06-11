package com.example.myapplication;

import com.example.myapplication.entity.Room;

import java.util.List;

public interface MessageReceiver {

    void receiveMessage(String messageJson);
}