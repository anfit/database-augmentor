package pl.edu.mimuw.dbaugmentor;

import pl.edu.mimuw.dbaugmentor.config.ApplicationProperties;
import pl.edu.mimuw.dbaugmentor.gui.GUI;
import pl.edu.mimuw.dbaugmentor.messages.Message;

import java.util.LinkedList;
import java.util.Queue;

public class Main {
    public static void main(String[] args) {
        try {
            ApplicationProperties properties = new ApplicationProperties();

            if (properties.isGuiEnabled()) {
                Queue<Message> msgQueue = new LinkedList<>();

                GUI gui = new GUI(properties, msgQueue);
                final Thread guiThread = new GuiThread(gui, msgQueue);

                Backend backend = new Backend(properties, msgQueue);
                final Thread backendThread = new BackendThread(backend, msgQueue);

                backendThread.start();
                guiThread.start();
            } else {
                Backend backend = new Backend(properties, null);
                backend.runValidationWithoutGui();
                backend.runCopyProcess();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
