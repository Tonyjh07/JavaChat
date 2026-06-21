package javachat.client.ui;

import javachat.client.ChatClient;
import javachat.client.listener.MessageListener;
import javachat.client.manager.FrameManager;
import javachat.common.ChatType;
import javachat.common.Command;
import javachat.common.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatFrame extends JFrame implements MessageListener {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JLabel statusBar;
    private ChatClient chatClient;
    private String name;
    private SimpleDateFormat timeFormat;
    private JScrollPane chatScrollPane;
    private ChatType chatType;
    private String sessionId;
    private String displayName;
    private FrameManager frameManager;

    public ChatFrame(ChatClient chatClient, ChatType chatType, String sessionId, String displayName) {
        this.chatClient = chatClient;
        this.name = chatClient.getName();
        this.timeFormat = new SimpleDateFormat("HH:mm:ss");
        this.chatType = chatType;
        this.sessionId = sessionId;
        this.displayName = displayName;
        this.frameManager = FrameManager.getInstance();

        initUI();
        setupListeners();

        String title = chatType == ChatType.GROUP ? "群聊 - " + displayName : "私聊 - " + displayName;
        setTitle(title);
        setSize(500, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        chatArea.setBackground(new Color(245, 245, 245));

        chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(chatScrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        inputField = new JTextField();
        inputField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        inputField.setPreferredSize(new Dimension(0, 30));

        sendButton = new JButton("发送");
        sendButton.setFocusPainted(false);
        sendButton.setPreferredSize(new Dimension(60, 32));

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        statusBar = new JLabel("已连接");
        statusBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusBar.setForeground(Color.GRAY);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusBar, BorderLayout.NORTH);

        inputField.requestFocus();
    }

    private void setupListeners() {
        sendButton.addActionListener(e -> sendMessage());

        inputField.addActionListener(e -> sendMessage());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frameManager.closeChat(sessionId);
            }
        });
    }

    private void sendMessage() {
        String content = inputField.getText().trim();

        if (content.isEmpty()) {
            return;
        }

        inputField.setText("");

        if (chatClient.isConnected()) {
            switch (chatType) {
                case GROUP:
                    chatClient.sendGroupMessage(content);
                    break;
                case PRIVATE:
                    chatClient.sendPrivateMessage(sessionId, content);
                    break;
            }
        } else {
            showStatus("连接已断开，无法发送消息", false);
        }

        inputField.requestFocus();
    }

    @Override
    public void onMessageReceived(Message message) {
        boolean shouldDisplay = false;

        switch (message.getCommand()) {
            case MESSAGE_GROUP:
                shouldDisplay = chatType == ChatType.GROUP;
                break;
            case PRIVATE_MSG:
                if (chatType == ChatType.PRIVATE) {
                    String sender = message.getSender();
                    String receiver = message.getReceiver();
                    shouldDisplay = sender.equals(sessionId) || receiver.equals(sessionId);
                }
                break;
            default:
                break;
        }

        if (shouldDisplay) {
            appendMessage(message);
        }
    }

    private String formatMessage(Message msg) {
        String time = timeFormat.format(new Date(msg.getTimestamp()));
        return String.format("[%s] %s：%s", time, msg.getSender(), msg.getContent());
    }

    private void appendMessage(Message msg) {
        String line = formatMessage(msg);
        chatArea.append(line + "\n");
        scrollToBottom();
    }

    private void appendSystemMessage(String content) {
        String time = timeFormat.format(new Date());
        String line = String.format("[%s] 【系统】%s", time, content);
        chatArea.append(line + "\n");
        scrollToBottom();
    }

    private void scrollToBottom() {
        chatScrollPane.getVerticalScrollBar().setValue(
                chatScrollPane.getVerticalScrollBar().getMaximum()
        );
    }

    private void showStatus(String message, boolean isConnected) {
        statusBar.setText(message);
        if (isConnected) {
            statusBar.setForeground(new Color(76, 175, 80));
        } else {
            statusBar.setForeground(new Color(244, 67, 54));
        }
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void onLoginResult(boolean success, String message) {
    }

    @Override
    public void onUserListUpdated(java.util.List<String> users) {
    }

    @Override
    public void onDisconnected() {
        SwingUtilities.invokeLater(() -> {
            showStatus("连接已断开", false);

            inputField.setEnabled(false);
            sendButton.setEnabled(false);

            appendSystemMessage("与服务器的连接已断开，请重新启动程序");
        });
    }

    @Override
    public void onConnectionStatusChanged(boolean connected, String reason) {
    }
}