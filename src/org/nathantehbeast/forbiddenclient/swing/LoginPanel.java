package org.nathantehbeast.forbiddenclient.swing;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.sql.*;

/**
 * Created by Nathan on 7/2/14.
 * http://www.powerbot.org/community/user/523484-nathan-l/
 * http://www.excobot.org/forum/user/906-nathan/
 */

public class LoginPanel extends JPanel {

    private final Application application;
    private final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    private String name, passhash, secret;
    private int groupId;

    public LoginPanel(Application application) {
        this.application = application;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(150, 150, 175, 150));

        JPanel nested0 = new JPanel(new GridLayout(3, 1, 50, 5));
        JPanel nested_username = new JPanel(new GridLayout());
        JPanel nested_pw = new JPanel(new GridLayout());

        JLabel username = new JLabel("Username");
        username.setFont(username.getFont().deriveFont(Font.BOLD));

        JLabel password = new JLabel("Password");
        password.setFont(password.getFont().deriveFont(Font.BOLD));

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        loginButton = new JButton("Login!");
        loginButton.setFont(loginButton.getFont().deriveFont(Font.BOLD));
        loginButton.addActionListener(e -> processLogin());

        nested_username.add(username);
        nested_username.add(usernameField);

        nested_pw.add(password);
        nested_pw.add(passwordField);

        TitledBorder titledBorder = new TitledBorder("Login");
        titledBorder.setTitleFont(titledBorder.getTitleFont().deriveFont(Font.BOLD));

        nested0.setBorder(titledBorder);
        nested0.add(nested_username);
        nested0.add(nested_pw);
        nested0.add(loginButton);

        add(nested0, BorderLayout.CENTER);
    }

    public JTextField getUsernameField() {
        return usernameField;
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    public void disableAll() {
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);
        loginButton.setEnabled(false);
    }

    public void enableAll() {
        usernameField.setEnabled(true);
        passwordField.setEnabled(true);
        loginButton.setEnabled(true);
    }

    private void processLogin() {
        disableAll();
        if (login(usernameField.getText(), new String(passwordField.getPassword()))) {
            application.setUser(name);
            application.setStatus("Welcome " + application.getUser() + "!");
            application.getTabbedPane().addTab("Overview", application.getOverviewPanel());
            if (groupId == 4 || groupId == 6 || groupId == 8)  {
                application.getTabbedPane().addTab("Management Tools", application.getManagementPanel());
                JOptionPane.showMessageDialog(null, "Login successful. Adding management panel.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Login successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Invalid login. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            enableAll();
        }
    }

    private boolean login(String user, String pwd) {
        Connection con;
        Statement statement;
        try {
            Class.forName("com.mysql.jdbc.Driver");

            con = DriverManager.getConnection("", "", "");

            statement = con.createStatement();

            PreparedStatement ps;
            ps = con.prepareStatement("SELECT name, member_group_id, members_pass_hash, members_pass_salt FROM ipb.ipb_members WHERE name = ? OR email = ?");

            ps.setString(1, user);
            ps.setString(2, user);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                name = rs.getString("name");
                groupId = rs.getInt("member_group_id");
                passhash = rs.getString("members_pass_hash");
                secret = rs.getString("members_pass_salt");
            }

            rs.close();
            statement.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return secret != null && passhash != null && pwd != null && checkHash(secret, passhash, pwd);
    }

    private boolean checkHash(String salt, String hash, String pwd) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(salt.getBytes("UTF-8"), 0, salt.length());
            String saltHash = toString(messageDigest.digest()).replace(" ", "").toLowerCase();

            messageDigest.update(pwd.getBytes("UTF-8"), 0, pwd.length());

            String mergedHash = saltHash + toString(messageDigest.digest()).replace(" ", "").toLowerCase();
            messageDigest.update(mergedHash.getBytes("UTF-8"), 0, mergedHash.length());

            String finalHash = toString(messageDigest.digest()).replace(" ", "").toLowerCase();
            return finalHash.equals(hash);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String toString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(toString(aByte));
            sb.append(' ');
        }
        return sb.substring(0, sb.length() - 1);
    }

    private String toString(byte byteValue) {
        int tmp = byteValue << 8;
        char[] chars = new char[]{HEX[(tmp >> 12) & 0x0F], HEX[(tmp >> 8) & 0x0F]};
        return String.valueOf(chars);
    }
}