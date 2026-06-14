package javachat.client;

import javax.swing.*;

public class ClientMain {
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            //
        }

        SwingUtilities.invokeLater(() -> {
            new LoginDialog().setVisible(true);
        });
    }
}
