package javachat.client;

import javachat.common.*;

import java.net.*;
import java.io.*;
import javax.swing.*;

public class ChatClient {
    private String host;
    private int port;
    private String name;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private volatile boolean connected = false;
    private MessageListener listener;
    private Thread receiveThread;

    public ChatClient() {
    }

    public ChatClient(MessageListener listener) {
        this.listener = listener;
    }

    public boolean connect(String host, int port, String name) {
        this.host = host;
        this.port = port;
        this.name = name;

        try {
            socket = new Socket(host, port);

            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();

            in = new ObjectInputStream(socket.getInputStream());

            Message loginMsg = new Message(name, Command.LOGIN, "");
            out.writeObject(loginMsg);
            out.flush();

            Message response = (Message) in.readObject();

            if (response.getCommand() == Command.LOGIN_SUCCESS) {

                connected = true;

                startReceiveThread();

                if (listener != null) {
                    listener.onLoginResult(true, response.getContent());
                }
                return true;
            } else {
                String failReason = response.getContent();
                if (listener != null) {
                    listener.onLoginResult(false, failReason);
                }
                close();
                return false;
            }

        } catch (Exception e) {
            if (listener != null) {
                listener.onLoginResult(false, "连接失败：" + e.getMessage());
            }
            close();
            return false;
        }
    }

    public void sendMessage(Message message) {
        if (!connected || out == null) {
            System.err.println("未连接，无法发送消息");
            return;
        }
        try {
            message.setSender(name);
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("发送消息失败：" + e.getMessage());
            if (listener != null) {
                listener.onDisconnected();
            }
            close();
        }
    }

    private void startReceiveThread() {
        receiveThread = new Thread(() -> {
            while (connected) {
                try {
                    Message message = (Message) in.readObject();

                    if (listener != null) {
                        SwingUtilities.invokeLater(() -> {
                            listener.onMessageReceived(message);
                        });
                    }
                } catch (EOFException e) {
                    System.out.println("连接已断开");
                    break;
                } catch (IOException | ClassNotFoundException e) {
                    if (connected) {
                        System.err.println("接收消息异常：" + e.getMessage());
                    }
                    break;
                }
            }

            if (connected) {
                close();
                if (listener != null) {
                    SwingUtilities.invokeLater(() -> {
                        listener.onDisconnected();
                    });
                }
            }
        });

        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    public void disconnect() {
        if (connected && out != null) {
            try {
                Message logoutMsg = new Message(name, Command.LOGOUT, "");
                out.writeObject(logoutMsg);
                out.flush();
            } catch (IOException e) {
            }
        }

        close();
    }

    public void close() {
        connected = false;

        try {
            if (in != null) in.close();
        } catch (IOException e) {
        }
        try {
            if (out != null) out.close();
        } catch (IOException e) {
        }
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public String getName() {
        return name;
    }

    public void setListener(MessageListener listener) {
        this.listener = listener;
    }
}