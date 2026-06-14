package javachat.client;

import javachat.common.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LoginDialog extends JDialog {

    private JTextField hostField;
    private JTextField portField;
    private JTextField nameField;
    private JButton connectButton;
    private JLabel statusLabel;
    private ChatClient chatClient;
    private boolean loginSuccess = false;

    public LoginDialog() {
        this(null);
    }

    public LoginDialog(JFrame owner) {
        super(owner, "登录聊天室", true);
        initUI();
        setupListeners();
        setupDefaults();
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);

        JLabel titleLabel = new JLabel("登录聊天室");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(titleLabel, gbc);

        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("服务器地址："), gbc);

        gbc.gridx = 1;
        hostField = new JTextField(15);
        add(hostField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("端口："), gbc);

        gbc.gridx = 1;
        portField = new JTextField(15);
        add(portField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("昵称："), gbc);

        gbc.gridx = 1;
        nameField = new JTextField(15);
        add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        connectButton = new JButton("连接服务器");
        add(connectButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        statusLabel = new JLabel("请输入服务器地址、端口和昵称");
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(statusLabel, gbc);
    }

    private void setupListeners() {
        connectButton.addActionListener(e -> onConnect());

        Action enterAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onConnect();
            }
        };

        hostField.addActionListener(e -> onConnect());
        portField.addActionListener(e -> onConnect());
        nameField.addActionListener(e -> onConnect());
    }

    private void onConnect() {
        String host = hostField.getText().trim();
        String portStr = portField.getText().trim();
        String name = nameField.getText().trim();

        if (host.isEmpty()) {
            showStatus("请输入服务器地址", false);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) {
                showStatus("端口号范围是 1-65535", false);
                return;
            }
        } catch (NumberFormatException e) {
            showStatus("端口号必须是数字", false);
            return;
        }

        if (name.isEmpty()) {
            showStatus("请输入昵称", false);
            return;
        }

        setInputEnabled(false);
        showStatus("正在连接服务器...", true);

        chatClient = new ChatClient(new LoginListener());

        new Thread(() -> {
            boolean success = chatClient.connect(host, port, name);
            if (!success) {
                SwingUtilities.invokeLater(() -> {
                    setInputEnabled(true);
                });
            }
        }).start();
    }

    private class LoginListener implements MessageListener {
        @Override
        public void onLoginResult(boolean success, String message) {
            SwingUtilities.invokeLater(() -> {
                if (success) {
                    showStatus("登录成功！正在打开聊天室...", true);
                    loginSuccess = true;

                    openChatFrame();

                    dispose();
                } else {
                    showStatus("登录失败：" + message, false);
                    setInputEnabled(true);
                }
            });
        }

        @Override
        public void onMessageReceived(Message message) {
        }

        @Override
        public void onDisconnected() {
            SwingUtilities.invokeLater(() -> {
                if (loginSuccess) {
                } else {
                    showStatus("连接已断开", false);
                    setInputEnabled(true);
                }
            });
        }
    }

    private void openChatFrame() {
        ChatFrame chatFrame = new ChatFrame(chatClient);
        chatFrame.setVisible(true);
    }


    private void showStatus(String message, boolean isGood) {
        statusLabel.setText(message);
        if (isGood) {
            statusLabel.setForeground(new Color(76, 175, 80));  // 绿色
        } else {
            statusLabel.setForeground(new Color(244, 67, 54));  // 红色
        }
    }

    private void setInputEnabled(boolean enabled) {
        hostField.setEnabled(enabled);
        portField.setEnabled(enabled);
        nameField.setEnabled(enabled);
        connectButton.setEnabled(enabled);
    }

    private void setupDefaults() {
        hostField.setText("127.0.0.1");
        portField.setText("7999");
        nameField.setText("");
        nameField.requestFocus();
    }

    @Override
    protected void dialogInit() {
        super.dialogInit();

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!loginSuccess) {
                    System.exit(0);
                }
            }
        });
    }
}
