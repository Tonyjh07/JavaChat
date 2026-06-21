package javachat.server;

import javachat.common.Command;
import javachat.common.Message;
import javachat.server.manager.ClientManager;
import javachat.server.router.MessageRouter;

import java.net.Socket;
import java.io.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String name;
    private final ClientManager clientManager;
    private final MessageRouter messageRouter;
    private volatile boolean running;

    public ClientHandler(Socket socket, ClientManager clientManager, MessageRouter messageRouter) {
        this.socket = socket;
        this.clientManager = clientManager;
        this.messageRouter = messageRouter;
        this.running = true;
    }

    @Override
    public void run() {
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());

            while (running) {
                Message message = (Message) in.readObject();

                switch (message.getCommand()) {
                    case LOGIN:
                        handleLogin(message);
                        break;
                    case MESSAGE_GROUP:
                    case PRIVATE_MSG:
                        messageRouter.route(message, this);
                        break;
                    case LOGOUT:
                        handleLogout();
                        break;
                    default:
                        break;
                }
            }
        } catch (EOFException e) {
            System.out.println("客户端" + (name != null ? " " + name : "") + "断开连接");
        } catch (IOException e) {
            if (running) {
                System.out.println("客户端" + (name != null ? " " + name : "") + "连接异常：" + e.getMessage());
            }
        } catch (ClassNotFoundException e) {
            System.out.println("客户端" + (name != null ? " " + name : "") + "消息解析异常：" + e.getMessage());
        } finally {
            close();
        }
    }

    private void handleLogin(Message message) throws IOException {
        String requestedName = message.getSender();

        if (clientManager.isNameExists(requestedName)) {
            Message failMsg = new Message("SERVER", Command.LOGIN_FAIL, "用户名已存在");
            sendMessage(failMsg);
            close();
        } else {
            this.name = requestedName;
            clientManager.addClient(name, this);

            Message successMsg = new Message("SERVER", Command.LOGIN_SUCCESS, name + "登录成功");
            sendMessage(successMsg);

            broadcastUserList();

            System.out.println("用户" + name + "已登录");
        }
    }

    private void handleLogout() throws IOException {
        System.out.println("用户" + name + "已退出");
        close();
    }

    private void broadcastUserList() {
        StringBuilder userList = new StringBuilder("群聊"); // 添加群聊入口
        for (String userName : clientManager.getAllNames()) {
            userList.append(",");
            userList.append(userName);
        }
        Message userListMsg = new Message("SERVER", Command.USER_LIST, userList.toString());
        clientManager.broadcastToAll(userListMsg);
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("用户" + name + "消息发送失败：" + e.getMessage());
            close();
        }
    }

    public void close() {
        if (!running) {
            return;
        }

        running = false;

        if (name != null) {
            clientManager.removeClient(name);
            broadcastUserList();
        }

        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            // 忽略关闭异常
        }

        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            // 忽略关闭异常
        }

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            // 忽略关闭异常
        }
    }

    public String getName() {
        return name;
    }
}