package pl.edu.mimuw.dbaugmentor.gui;

import pl.edu.mimuw.dbaugmentor.messages.Status;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class ProgressPanel extends JPanel {
    private final JLabel header;
    private final Map<Status, JCheckBox> statusToCheckBox = new HashMap<>();
    private final JLabel finish;

    public ProgressPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        header = new JLabel("Copying in progress, do not close the window.");
        add(header);
        for (Status status : Status.values()) {
            JCheckBox checkBox = new JCheckBox(status.toString());
            checkBox.setEnabled(false);
            checkBox.setVisible(true);
            statusToCheckBox.put(status, checkBox);
            add(checkBox);
        }
        finish = new JLabel("");
        add(finish);
        setVisible(true);
    }

    public void setCommitStepVisible(boolean show) {
        statusToCheckBox.get(Status.COMMIT).setVisible(show);
    }

    public void updateStatus(Status status) {
        statusToCheckBox.get(status).setSelected(true);
    }

    public void finishWithSuccess(String readableTime) {
        header.setText("");
        finish.setText("Copying successful, time elapsed: " + readableTime);
    }

    public void finishWithError() {
        header.setText("");
        finish.setText("Copying failed");
    }
}
