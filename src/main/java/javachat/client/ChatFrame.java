package javachat.client;

import javachat.common.Command;
import javachat.common.Message;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;

public class ChatFrame extends JFrame implements MessageListener {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JLabel statusBar;
    private ChatClient chatClient;
    private String name;
    private SimpleDateFormat timeFormat;
    private JScrollPane chatScrollPane;

    public ChatFrame(ChatClient chatClient) {
        this.chatClient = chatClient;
        this.name = chatClient.getName();
        this.timeFormat = new SimpleDateFormat("HH:mm:ss");

        chatClient.setListener(this);

        initUI();
        setupListeners();

        setTitle("聊天室 - " + name);
        setSize(500, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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
        sendButton.setBackground(new Color(33, 150, 243));
        sendButton.setForeground(Color.WHITE);
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
                onWindowClosing();
            }
        });
    }

    private void sendMessage() {
        String content = inputField.getText().trim();

        if (content.isEmpty()) {
            return;
        }

        inputField.setText("");

        Message msg = new Message(name, Command.MESSAGE_GROUP, content);


        if (chatClient.isConnected()) {
            chatClient.sendMessage(msg);
        } else {
            showStatus("连接已断开，无法发送消息", false);
        }

        inputField.requestFocus();
    }

    @Override
    public void onMessageReceived(Message message) {
        switch (message.getCommand()) {
            case MESSAGE_GROUP:
                appendMessage(message);
                break;

            default:
                break;
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

    private void onWindowClosing() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "确定要退出聊天室吗？",
                "退出确认",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            if (chatClient != null) {
                chatClient.disconnect();
            }
            System.exit(0);
        }
    }

    @Override
    public void onLoginResult(boolean success, String message) {
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
}