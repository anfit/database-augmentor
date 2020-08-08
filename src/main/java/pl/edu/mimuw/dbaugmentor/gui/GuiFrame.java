package pl.edu.mimuw.dbaugmentor.gui;

import pl.edu.mimuw.dbaugmentor.config.ApplicationProperties;
import pl.edu.mimuw.dbaugmentor.messages.Message;
import pl.edu.mimuw.dbaugmentor.messages.Status;

import javax.swing.*;
import java.awt.*;
import java.util.Queue;

public class GuiFrame extends JFrame {
    private final JTabbedPane tabbedPane;
    private final JButton button;
    private final ProgressPanel progressPanel;
    private final ApplicationProperties applicationProperties;

    public GuiFrame(ApplicationProperties applicationProperties, GUI gui, Queue<Message> msgQueue) {
        this.applicationProperties = applicationProperties;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("DB Augmentor");
        setSize(440,300);
        setResizable(false);
        setLocationRelativeTo(null);

        Panels panels = new Panels(applicationProperties);
        tabbedPane = new ConfigTabbedPane(panels);
        add(tabbedPane);
        button = new SubmitButton(applicationProperties, gui, panels, msgQueue);
        add(button, BorderLayout.SOUTH);
        setVisible(true);
        progressPanel = new ProgressPanel();
    }

    public void loadingScreen() {
        tabbedPane.setVisible(false);
        button.setVisible(false);

        progressPanel.setCommitStepVisible(applicationProperties.isCommit());
        add(progressPanel, BorderLayout.CENTER);
        progressPanel.revalidate();
    }

    public void updateStatus(Status status) {
        progressPanel.updateStatus(status);
    }

    public void finalScreen(String readableTime) {
        progressPanel.finishWithSuccess(readableTime);
    }

    public void showErrorScreen(String error) {
        progressPanel.finishWithError();
        ErrorDialog.show(error);
    }
}
