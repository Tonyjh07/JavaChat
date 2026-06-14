package javachat.client;

import javachat.common.Message;

public interface MessageListener {
    void onMessageReceived(Message message);

    void onLoginResult(boolean success, String message);

    void onDisconnected();
}
