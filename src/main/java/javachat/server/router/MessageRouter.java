package javachat.server.router;

import javachat.common.Command;
import javachat.common.Message;
import javachat.server.ClientHandler;
import javachat.server.manager.ClientManager;

public class MessageRouter {
    private final ClientManager clientManager;

    public MessageRouter(ClientManager clientManager) {
        this.clientManager = clientManager;
    }

    public void route(Message msg, ClientHandler sender) {
        if (msg == null || sender == null) {
            return;
        }

        Command command = msg.getCommand();
        if (command == null) {
            return;
        }

        switch (command) {
            case MESSAGE_GROUP:
                routeGroupMessage(msg, sender);
                break;
            case PRIVATE_MSG:
                routePrivateMessage(msg, sender);
                break;
            case SYSTEM_MSG:
                routeSystemMessage(msg);
                break;
            default:
                break;
        }
    }

    private void routeGroupMessage(Message msg, ClientHandler sender) {
        String senderName = sender.getName();
        Message broadcastMsg = new Message(senderName, Command.MESSAGE_GROUP, msg.getContent(), msg.getTimestamp());
        clientManager.broadcast(broadcastMsg, sender);
    }

    private void routePrivateMessage(Message msg, ClientHandler sender) {
        String receiverName = msg.getReceiver();

        if (receiverName == null || receiverName.isEmpty()) {
            sendErrorResponse(sender, "接收者不能为空");
            return;
        }

        String senderName = sender.getName();
        if (receiverName.equals(senderName)) {
            sendErrorResponse(sender, "不能给自己发私聊");
            return;
        }

        ClientHandler receiver = clientManager.getClient(receiverName);
        if (receiver == null) {
            sendErrorResponse(sender, "用户 " + receiverName + " 不在线");
            return;
        }

        Message privateMsg = new Message(senderName, Command.PRIVATE_MSG, msg.getContent(), msg.getTimestamp());
        privateMsg.setReceiver(receiverName);

        receiver.sendMessage(privateMsg);

        if (receiver != sender) {
            sender.sendMessage(privateMsg);
        }
    }

    private void routeSystemMessage(Message msg) {
        clientManager.broadcastToAll(msg);
    }

    public void sendErrorResponse(ClientHandler target, String errorMsg) {
        Message errorMessage = new Message("SERVER", Command.ERROR, errorMsg);
        target.sendMessage(errorMessage);
    }
}