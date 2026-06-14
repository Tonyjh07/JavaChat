package javachat.server;

import javachat.common.Message;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private int port;
    private boolean running = false;
    private ServerSocket serverSocket;
    private Map<String, ClientHandler> clients ;

    public ChatServer(int port){
        this.port = port;
        this.clients = new ConcurrentHashMap<>();
    }

    private ChatServer(){
    }

    public void start(){
        running = true;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("服务器启动成功，监听端口："+port);
            while (running){
                acceptClient();
            }
        } catch (IOException e) {
            System.err.println("服务器启动失败："+e.getMessage());
            e.printStackTrace();
        }
    }

    private void acceptClient(){
        try {
            Socket socket = serverSocket.accept();
            handleClient(socket);
        } catch (IOException e) {
            if (running) {
                System.err.println("客户端连接失败："+e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleClient(Socket socket){
        ClientHandler clientHandler = new ClientHandler(socket, this);
        new Thread(clientHandler).start();
    }

    public boolean isNameExist(String name) {
        return clients.containsKey(name);
    }

    public void addClient(ClientHandler handler) {
        String name = handler.getName();  // 需要在ClientHandler中添加getName()
        clients.put(name, handler);
        System.out.println("当前在线人数：" + clients.size());
    }

    public void removeClient(String name) {
        clients.remove(name);
        System.out.println("用户 " + name + " 已移除，当前在线人数：" + clients.size());
    }

    public void sendGroupMessage(Message message) {
        for (ClientHandler handler : clients.values()) {
            handler.sendMessage(message);
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("关闭服务器失败：" + e.getMessage());
        }
    }
}
