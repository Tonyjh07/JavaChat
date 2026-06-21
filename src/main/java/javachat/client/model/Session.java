package javachat.client.model;

import javachat.common.ChatType;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Session {
    private String sessionId;
    private ChatType sessionType;
    private String displayName;
    private String lastMessage;
    private long lastTime;
    private int unreadCount;
    private boolean isActive;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    public Session(String sessionId, ChatType sessionType, String displayName) {
        this.sessionId = sessionId;
        this.sessionType = sessionType;
        this.displayName = displayName;
        this.lastMessage = "";
        this.lastTime = System.currentTimeMillis();
        this.unreadCount = 0;
        this.isActive = false;
    }

    public String getSessionId() {
        return sessionId;
    }

    public ChatType getSessionType() {
        return sessionType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public long getLastTime() {
        return lastTime;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void updateLastMessage(String content, long timestamp) {
        this.lastMessage = content.length() > 30 ? content.substring(0, 30) + "..." : content;
        this.lastTime = timestamp;
    }

    public void incrementUnread() {
        unreadCount++;
    }

    public void clearUnread() {
        unreadCount = 0;
    }

    public String getLastTimeFormatted() {
        return timeFormat.format(new Date(lastTime));
    }

    public String getDisplayNameWithType() {
        String prefix = sessionType == ChatType.GROUP ? "[群]" : "[私]";
        return prefix + " " + displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}