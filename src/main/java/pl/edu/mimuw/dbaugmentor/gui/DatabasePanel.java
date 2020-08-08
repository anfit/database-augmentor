package pl.edu.mimuw.dbaugmentor.gui;

import pl.edu.mimuw.dbaugmentor.config.ApplicationProperties;

import javax.swing.*;

public class DatabasePanel extends JPanel {
    private final JTextField urlField;
    private final JTextField userField;
    private final JTextField passwordField;

    public DatabasePanel(ApplicationProperties applicationProperties) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel urlLbl = new JLabel("url");
        add(urlLbl);
        urlField = new JTextField(applicationProperties.getUrl(),35);
        add(urlField);

        JLabel userLbl = new JLabel("user");
        add(userLbl);
        userField = new JTextField(applicationProperties.getUser(),35);
        add(userField);

        JLabel passwordLbl = new JLabel("password");
        add(passwordLbl);
        passwordField = new JTextField(applicationProperties.getPassword(),35);
        add(passwordField);
    }

    public String getUrl() {
        return urlField.getText();
    }

    public String getUser() {
        return userField.getText();
    }

    public String getPassword() {
        return passwordField.getText();
    }
}
