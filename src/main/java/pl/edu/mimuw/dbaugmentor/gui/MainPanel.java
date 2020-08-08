package pl.edu.mimuw.dbaugmentor.gui;

import pl.edu.mimuw.dbaugmentor.config.ApplicationProperties;

import javax.swing.*;

public class MainPanel extends JPanel {
    private final JTextField tableNameField;
    private final JTextField multiplierField;
    private final JTextField sqlOutputField;
    private final JCheckBox cachingCheckBox;
    private final JCheckBox commitCheckBox;

    public MainPanel(ApplicationProperties applicationProperties) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel tableLbl = new JLabel("Table to start from");
        add(tableLbl);
        tableNameField = new JTextField(applicationProperties.getTableName(),35);
        add(tableNameField);

        JLabel multiplierLbl = new JLabel("Multiplier");
        add(multiplierLbl);
        multiplierField = new JTextField(String.valueOf(applicationProperties.getMultiplier()),35);
        add(multiplierField);

        JLabel sqlOutputLbl = new JLabel("File path for output SQL");
        add(sqlOutputLbl);
        sqlOutputField = new JTextField(applicationProperties.getSqlOutputFile(),35);
        add(sqlOutputField);

        cachingCheckBox = new JCheckBox("Cache the whole database", applicationProperties.isCaching());
        cachingCheckBox.setBounds(100,150, 50,50);
        add(cachingCheckBox);

        commitCheckBox = new JCheckBox("Commit transaction automatically", applicationProperties.isCommit());
        commitCheckBox.setBounds(100,150, 50,50);
        add(commitCheckBox);
    }

    public String getTableName() {
        return tableNameField.getText();
    }

    public int getMultiplier() throws NumberFormatException {
        return Integer.parseInt(multiplierField.getText());
    }

    public String getSqlOutputFile() {
        return sqlOutputField.getText();
    }

    public boolean isCaching() {
        return cachingCheckBox.isSelected();
    }

    public boolean isCommit() {
        return commitCheckBox.isSelected();
    }
}
