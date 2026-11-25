package org.example.agent.desktop;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Persistent floating top-bar indicator shown while a remote desktop session is active.
 * Guarantees transparency so the employee always knows their screen is being shared,
 * and allows immediate one-click disconnection by the employee.
 */
public class ActiveSessionOverlay {

    private static JWindow window;

    public static synchronized void showOverlay(String adminName, Runnable onDisconnect) {
        if (GraphicsEnvironment.isHeadless()) return;

        SwingUtilities.invokeLater(() -> {
            try {
                if (window != null) {
                    window.dispose();
                }

                window = new JWindow();
                window.setAlwaysOnTop(true);

                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 6));
                panel.setBackground(new Color(24, 24, 27)); // Dark zinc
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(239, 68, 68), 1), // Red border
                        new EmptyBorder(2, 10, 2, 8)
                ));

                JLabel dotLabel = new JLabel("●");
                dotLabel.setForeground(new Color(239, 68, 68)); // Pulsing red dot
                dotLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

                String name = (adminName != null && !adminName.isBlank()) ? adminName : "IT Admin";
                JLabel textLabel = new JLabel("Remote Session Active (" + name + ")");
                textLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                textLabel.setForeground(new Color(244, 244, 245));

                JButton discBtn = new JButton("Disconnect");
                discBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
                discBtn.setBackground(new Color(220, 38, 38));
                discBtn.setForeground(Color.WHITE);
                discBtn.setFocusPainted(false);
                discBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                discBtn.addActionListener(e -> {
                    hideOverlay();
                    if (onDisconnect != null) {
                        onDisconnect.run();
                    }
                });

                panel.add(dotLabel);
                panel.add(textLabel);
                panel.add(discBtn);

                window.setContentPane(panel);
                window.pack();

                Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                int x = (screen.width - window.getWidth()) / 2;
                window.setLocation(x, 10);
                window.setVisible(true);

            } catch (Exception e) {
                System.err.println("[ActiveOverlay] Failed to display overlay: " + e.getMessage());
            }
        });
    }

    public static synchronized void hideOverlay() {
        if (window != null) {
            SwingUtilities.invokeLater(() -> {
                try {
                    window.setVisible(false);
                    window.dispose();
                    window = null;
                } catch (Exception ignored) {}
            });
        }
    }
}
