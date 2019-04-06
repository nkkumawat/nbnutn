package me.nkkumawat.chatzzz.Model;

public class ChatMessage {
    public String message_type;
    public String message;
    public String message_time;
    public String media_type;
    public ChatMessage(String message_type, String message , String message_time , String media_type) {
        super();
        this.message = message;
        this.message_type = message_type;
        this.message_time = message_time;
        this.media_type = media_type;
    }

}
