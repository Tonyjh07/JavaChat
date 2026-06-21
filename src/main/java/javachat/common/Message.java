package javachat.common;

import java.io.Serializable;

public class Message implements Serializable {
    private String sender;
    private String receiver;
    private Command command;
    private String content;
    private long timestamp;

    public Message(String sender, Command command, String content) {
        this(sender, command, content, System.currentTimeMillis());
    }

    public Message(String sender, Command command, String content, long timestamp) {
        this.sender = sender;
        this.command = command;
        this.content = content;
        this.timestamp = timestamp;
    }

    private Message() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}