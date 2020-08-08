package pl.edu.mimuw.dbaugmentor.gui;

import javax.swing.*;

public class ErrorDialog {
    public static void show(String errorMsg) {
        JOptionPane.showMessageDialog(new JFrame(),
                errorMsg,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
