package org.example.agent.desktop;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Modern Swing modal dialog presented to the employee when an administrator
 * requests remote desktop access. Prevents unauthorized screen viewing.
 * Auto-rejects after 60 seconds if unattended.
 */
public class EmployeeApprovalDialog {

    public static boolean requestApproval(String adminName, String adminEmail, String serialNumber) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("[EmployeeApproval] Running in headless environment. Cannot display approval dialog. Auto-rejecting.");
            return false;
        }

        final AtomicBoolean approved = new AtomicBoolean(false);
        final CountDownLatch latch = new CountDownLatch(1);

        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}

                JDialog dialog = new JDialog((Frame) null, "Remote Desktop Access Request", true);
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dialog.setAlwaysOnTop(true);
                dialog.setResizable(false);

                JPanel root = new JPanel(new BorderLayout(15, 15));
                root.setBorder(new EmptyBorder(20, 25, 20, 25));
                root.setBackground(Color.WHITE);

                // Header
                JPanel headerPanel = new JPanel(new BorderLayout(10, 5));
                headerPanel.setBackground(Color.WHITE);

                JLabel titleLabel = new JLabel("IT Remote Support Request");
                titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
                titleLabel.setForeground(new Color(24, 43, 73));
                headerPanel.add(titleLabel, BorderLayout.NORTH);

                JLabel subLabel = new JLabel("An administrator has requested remote access to your laptop.");
                subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                subLabel.setForeground(new Color(100, 116, 139));
                headerPanel.add(subLabel, BorderLayout.SOUTH);

                root.add(headerPanel, BorderLayout.NORTH);

                // Body Card
                JPanel bodyPanel = new JPanel(new GridLayout(3, 1, 8, 8));
                bodyPanel.setBackground(new Color(248, 250, 252));
                bodyPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                        new EmptyBorder(12, 16, 12, 16)
                ));

                JLabel adminLabel = new JLabel("Administrator:  " + (adminName != null ? adminName : "IT Administrator"));
                adminLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                adminLabel.setForeground(new Color(30, 41, 59));

                JLabel emailLabel = new JLabel("Email:  " + (adminEmail != null ? adminEmail : "N/A"));
                emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                emailLabel.setForeground(new Color(71, 85, 105));

                JLabel noticeLabel = new JLabel("You will be able to terminate this session at any time.");
                noticeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                noticeLabel.setForeground(new Color(37, 99, 235));

                bodyPanel.add(adminLabel);
                bodyPanel.add(emailLabel);
                bodyPanel.add(noticeLabel);

                root.add(bodyPanel, BorderLayout.CENTER);

                // Bottom Panel with Countdown and Action Buttons
                JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
                bottomPanel.setBackground(Color.WHITE);

                JLabel timerLabel = new JLabel("Auto-declining in 60s...");
                timerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                timerLabel.setForeground(new Color(148, 163, 184));
                bottomPanel.add(timerLabel, BorderLayout.WEST);

                JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
                btnGroup.setBackground(Color.WHITE);

                JButton rejectBtn = new JButton("Decline");
                rejectBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                rejectBtn.setBackground(new Color(241, 245, 249));
                rejectBtn.setForeground(new Color(71, 85, 105));
                rejectBtn.setFocusPainted(false);
                rejectBtn.setPreferredSize(new Dimension(95, 34));
                rejectBtn.addActionListener(e -> {
                    approved.set(false);
                    dialog.dispose();
                    latch.countDown();
                });

                JButton approveBtn = new JButton("Allow Access");
                approveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                approveBtn.setBackground(new Color(37, 99, 235));
                approveBtn.setForeground(Color.WHITE);
                approveBtn.setFocusPainted(false);
                approveBtn.setPreferredSize(new Dimension(115, 34));
                approveBtn.addActionListener(e -> {
                    approved.set(true);
                    dialog.dispose();
                    latch.countDown();
                });

                btnGroup.add(rejectBtn);
                btnGroup.add(approveBtn);
                bottomPanel.add(btnGroup, BorderLayout.EAST);

                root.add(bottomPanel, BorderLayout.SOUTH);

                dialog.setContentPane(root);
                dialog.pack();
                dialog.setLocationRelativeTo(null);

                // 60-second countdown timer
                final int[] remainingSeconds = {60};
                Timer countdownTimer = new Timer(1000, null);
                countdownTimer.addActionListener(e -> {
                    remainingSeconds[0]--;
                    if (remainingSeconds[0] <= 0) {
                        countdownTimer.stop();
                        approved.set(false);
                        dialog.dispose();
                        latch.countDown();
                    } else {
                        timerLabel.setText("Auto-declining in " + remainingSeconds[0] + "s...");
                    }
                });
                countdownTimer.start();

                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        countdownTimer.stop();
                        latch.countDown();
                    }
                });

                dialog.setVisible(true);

            } catch (Exception e) {
                System.err.println("[EmployeeApproval] Error showing dialog: " + e.getMessage());
                latch.countDown();
            }
        });

        try {
            boolean completed = latch.await(65, TimeUnit.SECONDS);
            if (!completed) {
                approved.set(false);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            approved.set(false);
        }

        return approved.get();
    }
}
