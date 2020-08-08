package pl.edu.mimuw.dbaugmentor.gui;

import pl.edu.mimuw.dbaugmentor.config.ApplicationProperties;

public class Panels {
    private final DatabasePanel databasePanel;
    private final MainPanel mainPanel;
    private final AdvancedPanel advancedPanel;

    public Panels(ApplicationProperties applicationProperties) {
        databasePanel = new DatabasePanel(applicationProperties);
        mainPanel = new MainPanel(applicationProperties);
        advancedPanel = new AdvancedPanel(applicationProperties);
    }

    public DatabasePanel getDatabasePanel() {
        return databasePanel;
    }

    public MainPanel getMainPanel() {
        return mainPanel;
    }

    public AdvancedPanel getAdvancedPanel() {
        return advancedPanel;
    }
}
