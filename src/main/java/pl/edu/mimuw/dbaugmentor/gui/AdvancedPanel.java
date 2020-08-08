package pl.edu.mimuw.dbaugmentor.gui;

import pl.edu.mimuw.dbaugmentor.config.ApplicationProperties;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;

public class AdvancedPanel extends JPanel {
    private final JTextField missingFkField;
    private final JTextArea constraintsToSkipField;
    private final JCheckBox ignoreFkConstraintsCheckBox;
    private final JCheckBox optimizeUniqueValueSearchCheckBox;
    private final JCheckBox readableStringCheckBox;
    private final JCheckBox uniqueKeyCacheCheckBox;

    public AdvancedPanel(ApplicationProperties applicationProperties) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel missingFkLbl = new JLabel("File with missing foreign keys");
        add(missingFkLbl);
        missingFkField = new JTextField(applicationProperties.getMissingFkFile(),35);
        add(missingFkField);

        JLabel constraintsToSkipLbl = new JLabel("Unique constraints to skip");
        add(constraintsToSkipLbl);
        constraintsToSkipField = new JTextArea(3,35);
        constraintsToSkipField.setWrapStyleWord(true);
        initTextArea(applicationProperties.getUniqueConstraintsToSkip());
        add(new JScrollPane(constraintsToSkipField));

        ignoreFkConstraintsCheckBox =
                new JCheckBox("Ignore foreign key constraints", applicationProperties.isIgnoringFkConstraints());
        ignoreFkConstraintsCheckBox.setBounds(100,150, 50,50);
        add(ignoreFkConstraintsCheckBox);

        optimizeUniqueValueSearchCheckBox =
                new JCheckBox(
                        "Optimize unique value search", applicationProperties.isOptimizingUniqueValueSearch());
        optimizeUniqueValueSearchCheckBox.setBounds(100,150, 50,50);
        add(optimizeUniqueValueSearchCheckBox);

        readableStringCheckBox =
                new JCheckBox(
                        "Generate readable strings (append copy number)",
                        applicationProperties.isGeneratingReadableString());
        readableStringCheckBox.setBounds(100,150, 50,50);
        add(readableStringCheckBox);

        uniqueKeyCacheCheckBox =
                new JCheckBox(
                        "Cache unique key values",
                        applicationProperties.isCachingUniqueKey());
        uniqueKeyCacheCheckBox.setBounds(100,150, 50,50);
        add(uniqueKeyCacheCheckBox);
    }

    private void initTextArea(ArrayList<String> lines) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String line: lines) {
            stringBuilder.append(line).append("\n");
        }
        constraintsToSkipField.setText(stringBuilder.toString());
    }

    public String getMissingFkFile() {
        return missingFkField.getText();
    }

    public ArrayList<String> getUniqueConstraintsToSkip() {
        ArrayList<String> result = new ArrayList<>();
        Collections.addAll(result, constraintsToSkipField .getText().split("\n"));
        return result;
    }

    public boolean isIgnoringFkConstraints() {
        return ignoreFkConstraintsCheckBox.isSelected();
    }

    public boolean isOptimizingUniqueValueSearch() {
        return optimizeUniqueValueSearchCheckBox.isSelected();
    }

    public boolean isGeneratingReadableStrings() {
        return readableStringCheckBox.isSelected();
    }

    public boolean isCachingUniqueKeys() {
        return uniqueKeyCacheCheckBox.isSelected();
    }
}
