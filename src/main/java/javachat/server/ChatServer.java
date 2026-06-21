package javachat.server;

import javachat.server.manager.ClientManager;
import javachat.server.router.MessageRouter;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

public class ChatServer {
    private int port;
    private boolean running = false;
    private ServerSocket serverSocket;
    private ClientManager clientManager;
    private MessageRouter messageRouter;

    public ChatServer(int port) {
        this.port = port;
        this.clientManager = new ClientManager();
        this.messageRouter = new MessageRouter(clientManager);
    }

    public void start() {
        running = true;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("服务器启动成功，监听端口：" + port);
            while (running) {
                acceptClient();
            }
        } catch (IOException e) {
            System.err.println("服务器启动失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void acceptClient() {
        try {
            Socket socket = serverSocket.accept();
            handleClient(socket);
        } catch (IOException e) {
            if (running) {
                System.err.println("客户端连接失败：" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleClient(Socket socket) {
        ClientHandler clientHandler = new ClientHandler(socket, clientManager, messageRouter);
        new Thread(clientHandler).start();
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