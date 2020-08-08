package pl.edu.mimuw.dbaugmentor.gui;

import pl.edu.mimuw.dbaugmentor.config.ApplicationProperties;
import pl.edu.mimuw.dbaugmentor.messages.Message;
import pl.edu.mimuw.dbaugmentor.messages.ValidationRequest;
import pl.edu.mimuw.dbaugmentor.messages.ValidationResponse;
import pl.edu.mimuw.dbaugmentor.messages.ValidationStatus;

import javax.swing.*;
import java.util.Queue;

public class SubmitButton extends JButton {
    public SubmitButton(
            ApplicationProperties applicationProperties, GUI gui, Panels panels, Queue<Message> msgQueue) {
        setText("Copy!");
        addActionListener(actionEvent -> {
            DatabasePanel databasePanel = panels.getDatabasePanel();
            applicationProperties.setUrl(databasePanel.getUrl());
            applicationProperties.setUser(databasePanel.getUser());
            applicationProperties.setPassword(databasePanel.getPassword());

            AdvancedPanel advancedPanel = panels.getAdvancedPanel();
            applicationProperties.setMissingFkFile(advancedPanel.getMissingFkFile());
            applicationProperties.setUniqueConstraintsToSkip(advancedPanel.getUniqueConstraintsToSkip());
            applicationProperties.setIgnoreFkConstraints(advancedPanel.isIgnoringFkConstraints());
            applicationProperties.setOptimizeUniqueValueSearch(advancedPanel.isOptimizingUniqueValueSearch());
            applicationProperties.setReadableStrings(advancedPanel.isGeneratingReadableStrings());

            MainPanel mainPanel = panels.getMainPanel();
            applicationProperties.setTableName(mainPanel.getTableName());
            applicationProperties.setSqlOutputFile(mainPanel.getSqlOutputFile());
            applicationProperties.setCaching(mainPanel.isCaching());
            applicationProperties.setCommit(mainPanel.isCommit());
            try {
                applicationProperties.setMultiplier(mainPanel.getMultiplier());
            } catch (NumberFormatException e) {
                ErrorDialog.show(ValidationStatus.MULTIPLIER_INT.toString());
                return;
            }

            if (mainPanel.getMultiplier() < 2) {
                ErrorDialog.show(ValidationStatus.MULTIPLIER.toString());
                return;
            }

            synchronized (msgQueue) {
                msgQueue.add(new ValidationRequest());
                msgQueue.notify();
            }

            synchronized (msgQueue) {
                try {
                    msgQueue.wait();
                    ValidationResponse validationResponse = (ValidationResponse) msgQueue.poll();
                    if (validationResponse.isSuccess()) {
                        synchronized (gui) {
                            gui.notify();
                        }
                    } else {
                        ErrorDialog.show(validationResponse.getErrorMsg());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
