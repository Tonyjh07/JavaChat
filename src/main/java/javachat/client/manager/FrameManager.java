package javachat.client.manager;

import javachat.client.ChatClient;
import javachat.client.ui.ChatFrame;
import javachat.common.ChatType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FrameManager {
    private static volatile FrameManager instance;
    private ChatClient chatClient;
    private final Map<String, ChatFrame> chatFrames;
    private final Object lock = new Object();

    private FrameManager() {
        chatFrames = new ConcurrentHashMap<>();
    }

    public static FrameManager getInstance() {
        if (instance == null) {
            synchronized (FrameManager.class) {
                if (instance == null) {
                    instance = new FrameManager();
                }
            }
        }
        return instance;
    }

    public void init(ChatClient client) {
        this.chatClient = client;
    }

    public void openChat(String sessionId, ChatType type, String displayName) {
        if (chatFrames.containsKey(sessionId)) {
            focusChat(sessionId);
            return;
        }

        ChatFrame frame = new ChatFrame(chatClient, type, sessionId, displayName);
        chatFrames.put(sessionId, frame);
        frame.setVisible(true);
    }

    public void closeChat(String sessionId) {
        ChatFrame frame = chatFrames.remove(sessionId);
        if (frame != null) {
            frame.dispose();
        }
    }

    public void focusChat(String sessionId) {
        ChatFrame frame = chatFrames.get(sessionId);
        if (frame != null) {
            frame.toFront();
            frame.requestFocus();
        }
    }

    public boolean isChatOpen(String sessionId) {
        return chatFrames.containsKey(sessionId);
    }

    public void closeAllWindows() {
        for (ChatFrame frame : chatFrames.values()) {
            frame.dispose();
        }
        chatFrames.clear();
    }

    public ChatFrame getChatFrame(String sessionId) {
        return chatFrames.get(sessionId);
    }

    public ChatClient getChatClient() {
        return chatClient;
    }
}