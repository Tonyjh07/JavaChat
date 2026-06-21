package javachat.client.listener;

import javachat.common.Message;

import java.util.List;

public interface MessageListener {
    void onMessageReceived(Message msg);

    void onUserListUpdated(List<String> users);

    void onLoginResult(boolean success, String reason);

    void onDisconnected();

    void onConnectionStatusChanged(boolean connected, String reason);
}