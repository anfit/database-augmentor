package pl.edu.mimuw.dbaugmentor.gui;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class ConfigTabbedPane extends JTabbedPane {
    public ConfigTabbedPane(Panels panels) {
        addTab("Main config", null, panels.getMainPanel(), "copying options");
        setMnemonicAt(0, KeyEvent.VK_1);

        addTab(
                "Database credentials", null, panels.getDatabasePanel(), "must be set to connect to a database");
        setMnemonicAt(1, KeyEvent.VK_2);

        addTab("Advanced config", null, panels.getAdvancedPanel(), "advanced config");
        setMnemonicAt(2, KeyEvent.VK_3);
    }
}
