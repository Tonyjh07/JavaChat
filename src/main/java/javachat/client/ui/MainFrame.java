package javachat.client.ui;

import javachat.client.ChatClient;
import javachat.client.manager.FrameManager;
import javachat.client.model.Session;
import javachat.client.listener.MessageListener;
import javachat.common.ChatType;
import javachat.common.Command;
import javachat.common.Message;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame implements MessageListener {
    private ChatClient chatClient;
    private FrameManager frameManager;
    private JLabel nicknameLabel;
    private JLabel statusLabel;
    private JTextField searchField;
    private DefaultListModel<Session> sessionListModel;
    private JList<Session> sessionList;
    private DefaultListModel<String> onlineUserModel;
    private JList<String> onlineUserList;
    private JPanel userInfoPanel;
    private Map<String, Session> sessionMap;

    public MainFrame(ChatClient chatClient) {
        this.chatClient = chatClient;
        this.frameManager = FrameManager.getInstance();
        this.sessionMap = new LinkedHashMap<>();
        this.sessionListModel = new DefaultListModel<>();
        this.onlineUserModel = new DefaultListModel<>();

        initUI();
        initListeners();

        setTitle("JavaChat - " + chatClient.getName());
        setSize(320, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BorderLayout(10, 5));
        userInfoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        userInfoPanel.setBackground(new Color(66, 133, 244));

        JLabel avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(40, 40));
        avatarLabel.setOpaque(true);
        avatarLabel.setBackground(Color.WHITE);
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        avatarLabel.setForeground(new Color(66, 133, 244));
        avatarLabel.setText(chatClient.getName().substring(0, 1));

        nicknameLabel = new JLabel(chatClient.getName());
        nicknameLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        nicknameLabel.setForeground(Color.WHITE);

        statusLabel = new JLabel("在线");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(200, 230, 255));

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setOpaque(false);
        namePanel.add(nicknameLabel);
        namePanel.add(statusLabel);

        userInfoPanel.add(avatarLabel, BorderLayout.WEST);
        userInfoPanel.add(namePanel, BorderLayout.CENTER);

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        searchPanel.setBackground(Color.WHITE);

        searchField = new JTextField();
        searchField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        searchField.setForeground(Color.GRAY);
        searchField.setText("搜索会话/用户");
        searchPanel.add(searchField, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(180);
        splitPane.setDividerSize(1);

        sessionList = new JList<>(sessionListModel);
        sessionList.setCellRenderer(new SessionListRenderer());
        sessionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sessionList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        sessionList.setBackground(Color.WHITE);

        JScrollPane sessionScrollPane = new JScrollPane(sessionList);
        sessionScrollPane.setBorder(null);

        JPanel sessionPanel = new JPanel(new BorderLayout());
        sessionPanel.add(createSectionHeader("消息"), BorderLayout.NORTH);
        sessionPanel.add(sessionScrollPane, BorderLayout.CENTER);

        onlineUserList = new JList<>(onlineUserModel);
        onlineUserList.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        onlineUserList.setBackground(Color.WHITE);
        onlineUserList.setCellRenderer(new OnlineUserRenderer());

        JScrollPane userScrollPane = new JScrollPane(onlineUserList);
        userScrollPane.setBorder(null);

        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.add(createSectionHeader("在线用户"), BorderLayout.NORTH);
        userPanel.add(userScrollPane, BorderLayout.CENTER);

        splitPane.setTopComponent(sessionPanel);
        splitPane.setBottomComponent(userPanel);

        add(userInfoPanel, BorderLayout.NORTH);
        add(searchPanel, BorderLayout.CENTER);
        add(splitPane, BorderLayout.SOUTH);

        Session groupSession = new Session("GROUP", ChatType.GROUP, "群聊");
        sessionMap.put("GROUP", groupSession);
        sessionListModel.addElement(groupSession);
    }

    private JPanel createSectionHeader(String title) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        headerPanel.setBackground(new Color(245, 245, 245));

        JLabel headerLabel = new JLabel(title);
        headerLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        headerLabel.setForeground(Color.GRAY);
        headerPanel.add(headerLabel, BorderLayout.WEST);

        return headerPanel;
    }

    private void initListeners() {
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("搜索会话/用户")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("搜索会话/用户");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterSessions(searchField.getText().trim());
            }
        });

        sessionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Session session = sessionList.getSelectedValue();
                    if (session != null) {
                        openChat(session);
                    }
                }
            }
        });

        onlineUserList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String userName = onlineUserList.getSelectedValue();
                    if (userName != null) {
                        // 群聊入口：双击"群聊"或"GROUP"打开群聊
                        if (userName.equals("群聊") || userName.equals("GROUP")) {
                            Session groupSession = sessionMap.get("GROUP");
                            if (groupSession != null) {
                                openChat(groupSession);
                            }
                            return;
                        }
                        // 私聊入口：双击其他用户打开私聊
                        if (!userName.equals(chatClient.getName())) {
                            Session session = sessionMap.get(userName);
                            if (session == null) {
                                session = new Session(userName, ChatType.PRIVATE, userName);
                                sessionMap.put(userName, session);
                                sessionListModel.addElement(session);
                            }
                            openChat(session);
                        }
                    }
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                        MainFrame.this,
                        "确定要退出聊天室吗？",
                        "退出确认",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (choice == JOptionPane.YES_OPTION) {
                    if (chatClient != null) {
                        chatClient.disconnect();
                    }
                    frameManager.closeAllWindows();
                    System.exit(0);
                }
            }
        });
    }

    private void openChat(Session session) {
        session.clearUnread();
        session.setActive(true);
        sessionList.repaint();

        frameManager.openChat(session.getSessionId(), session.getSessionType(), session.getDisplayName());
    }

    private void filterSessions(String keyword) {
        sessionListModel.clear();
        for (Session session : sessionMap.values()) {
            if (keyword.isEmpty() ||
                    session.getDisplayName().toLowerCase().contains(keyword.toLowerCase())) {
                sessionListModel.addElement(session);
            }
        }
    }

    public void addOrUpdateSession(Message msg) {
        String sessionId;
        String displayName;
        ChatType chatType;

        if (msg.getCommand() == Command.MESSAGE_GROUP) {
            sessionId = "GROUP";
            displayName = "群聊";
            chatType = ChatType.GROUP;
        } else {
            String sender = msg.getSender();
            String receiver = msg.getReceiver();
            String targetName = sender.equals(chatClient.getName()) ? receiver : sender;
            sessionId = targetName;
            displayName = targetName;
            chatType = ChatType.PRIVATE;
        }

        Session session = sessionMap.get(sessionId);
        if (session == null) {
            session = new Session(sessionId, chatType, displayName);
            sessionMap.put(sessionId, session);
            sessionListModel.addElement(session);
        }

        session.updateLastMessage(msg.getContent(), msg.getTimestamp());

        if (!session.isActive()) {
            session.incrementUnread();
        }

        reorderSessions();
        sessionList.repaint();
    }

    private void reorderSessions() {
        List<Session> sessions = new ArrayList<>(sessionMap.values());
        sessions.sort((s1, s2) -> Long.compare(s2.getLastTime(), s1.getLastTime()));

        sessionListModel.clear();
        for (Session session : sessions) {
            sessionListModel.addElement(session);
        }
    }

    @Override
    public void onMessageReceived(Message message) {
        switch (message.getCommand()) {
            case MESSAGE_GROUP:
            case PRIVATE_MSG:
                addOrUpdateSession(message);
                break;
            case USER_LIST:
                onUserListUpdated(Arrays.asList(message.getContent().split(",")));
                break;
            case ERROR:
                JOptionPane.showMessageDialog(this, message.getContent(), "错误", JOptionPane.ERROR_MESSAGE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onUserListUpdated(List<String> users) {
        onlineUserModel.clear();
        for (String user : users) {
            onlineUserModel.addElement(user);
        }
    }

    @Override
    public void onLoginResult(boolean success, String message) {
    }

    @Override
    public void onDisconnected() {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("离线");
            JOptionPane.showMessageDialog(this, "与服务器的连接已断开", "连接断开", JOptionPane.WARNING_MESSAGE);
        });
    }

    @Override
    public void onConnectionStatusChanged(boolean connected, String reason) {
        statusLabel.setText(connected ? "在线" : "离线");
    }

    private static class SessionListRenderer extends DefaultListCellRenderer {
        private static final Border SELECTED_BORDER = BorderFactory.createLineBorder(new Color(66, 133, 244), 1);

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Session session) {
                setText(session.getDisplayNameWithType());

                JPanel panel = new JPanel(new BorderLayout(5, 5));
                panel.setBorder(new EmptyBorder(8, 10, 8, 10));
                panel.setBackground(isSelected ? new Color(230, 245, 255) : Color.WHITE);

                JLabel avatarLabel = new JLabel();
                avatarLabel.setPreferredSize(new Dimension(40, 40));
                avatarLabel.setOpaque(true);
                avatarLabel.setBackground(session.getSessionType() == ChatType.GROUP ?
                        new Color(239, 154, 154) : new Color(186, 189, 182));
                avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
                avatarLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
                avatarLabel.setForeground(Color.WHITE);
                avatarLabel.setText(session.getDisplayName().substring(0, 1));

                JPanel contentPanel = new JPanel();
                contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
                contentPanel.setOpaque(false);

                JLabel nameLabel = new JLabel(session.getDisplayName());
                nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
                nameLabel.setForeground(Color.BLACK);

                JLabel messageLabel = new JLabel(session.getLastMessage());
                messageLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                messageLabel.setForeground(Color.GRAY);

                contentPanel.add(nameLabel);
                contentPanel.add(messageLabel);

                JPanel rightPanel = new JPanel();
                rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
                rightPanel.setOpaque(false);
                rightPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);

                JLabel timeLabel = new JLabel(session.getLastTimeFormatted());
                timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
                timeLabel.setForeground(Color.LIGHT_GRAY);

                rightPanel.add(timeLabel);

                if (session.getUnreadCount() > 0) {
                    JLabel unreadLabel = new JLabel(String.valueOf(session.getUnreadCount()));
                    unreadLabel.setFont(new Font("微软雅黑", Font.BOLD, 11));
                    unreadLabel.setForeground(Color.WHITE);
                    unreadLabel.setBackground(new Color(244, 67, 54));
                    unreadLabel.setOpaque(true);
                    unreadLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    unreadLabel.setPreferredSize(new Dimension(18, 18));
                    unreadLabel.setBorder(new EmptyBorder(0, 4, 0, 4));
                    rightPanel.add(unreadLabel);
                }

                panel.add(avatarLabel, BorderLayout.WEST);
                panel.add(contentPanel, BorderLayout.CENTER);
                panel.add(rightPanel, BorderLayout.EAST);

                if (isSelected) {
                    panel.setBorder(SELECTED_BORDER);
                }

                return panel;
            }

            return this;
        }
    }

    private static class OnlineUserRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof String userName) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
                panel.setBackground(isSelected ? new Color(230, 245, 255) : Color.WHITE);

                JLabel statusDot = new JLabel("●");
                statusDot.setFont(new Font("微软雅黑", Font.BOLD, 16));
                statusDot.setForeground(new Color(76, 175, 80));

                JLabel nameLabel = new JLabel(userName);
                nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
                nameLabel.setForeground(Color.BLACK);

                panel.add(statusDot);
                panel.add(nameLabel);

                return panel;
            }

            return this;
        }
    }
}