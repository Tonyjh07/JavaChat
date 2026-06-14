package javachat.server;

import javachat.common.Command;
import javachat.common.Message;
import javachat.server.ChatServer;

import java.net.*;
import java.io.*;
import java.util.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String name;
    private ChatServer server;
    private volatile boolean running;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
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
                        handleGroupMessage(message);
                        break;
                    case LOGOUT:
                        handleLogout();
                        break;
                    default:
                        break;
                }
            }
        } catch (EOFException e) {
            System.out.println("客户端"+(name != null ? " " + name : "")+"断开连接");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("客户端"+(name != null ? " " + name : "")+"连接异常："+e.getMessage());
            e.printStackTrace();
        } finally {
            close();
        }
    }

    private void handleLogin(Message message) throws IOException {
        String requestedName = message.getSender();

        if (server.isNameExist(requestedName)) {
            Message failMsg = new Message("SERVER", Command.LOGIN_FAIL, "用户名已存在");
            sendMessage(failMsg);

            close();
        } else {
            this.name = requestedName;
            server.addClient(this);

            Message successMsg = new Message("SERVER", Command.LOGIN_SUCCESS, name+"登录成功");
            sendMessage(successMsg);

            System.out.println("用户"+name+"已登录");
        }
    }

    private void handleGroupMessage(Message message) throws IOException {
        message.setSender(name); //防止客户端伪造发送者信息

        server.sendGroupMessage(message);
    }

    private void handleLogout() throws IOException {
        System.out.println("用户"+name+"已退出");
        close();
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("用户"+name+"消息发送失败："+e.getMessage());
            close();
        }
    }

    public void close() {
        if (!running) { //幂等性处理
            return;
        }

        running = false;

        if (name != null){
            server.removeClient(name);
        }

        try {
            if (in != null){
                in.close();
            }
        }catch (IOException e) {
            System.err.println("用户"+name+"输入流关闭失败："+e.getMessage());
            e.printStackTrace();
        }

        try {
            if (out != null){
                out.close();
            }
        }catch (IOException e) {
            System.err.println("用户"+name+"输出流关闭失败："+e.getMessage());
            e.printStackTrace();
        }

        try {
            if (socket != null){
                socket.close();
            }
        }catch (IOException e) {
            System.err.println("用户"+name+"Socket关闭失败："+e.getMessage());
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }
}
