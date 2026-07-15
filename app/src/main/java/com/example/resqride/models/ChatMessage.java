package com.example.resqride.models;

public class ChatMessage {

    public String id;
    public String senderId;
    public String senderName;

    public String receiverId;

    public String message;
    public String type; // text, image, file

    public String fileUrl;
    public String fileName;

    public boolean seen;
    public long time;

    public ChatMessage(){}

    public boolean isMine(String myId){
        if(senderId == null || myId == null)
            return false;

        return senderId.equals(myId);
    }
}