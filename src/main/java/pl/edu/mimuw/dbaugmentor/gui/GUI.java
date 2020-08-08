package pl.edu.mimuw.dbaugmentor.gui;

import pl.edu.mimuw.dbaugmentor.config.ApplicationProperties;
import pl.edu.mimuw.dbaugmentor.messages.Message;
import pl.edu.mimuw.dbaugmentor.messages.Status;

import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class GUI {
    private final GuiFrame guiFrame;

    public GUI(ApplicationProperties applicationProperties, Queue<Message> msgQueue) {
        guiFrame = new GuiFrame(applicationProperties, this, msgQueue);
    }

    public void loadingScreen() {
        guiFrame.loadingScreen();
    }

    public void updateLoadingScreen(Status status) {
        guiFrame.updateStatus(status);
    }

    public void finalScreen(long timeElapsed) {
        String readableTime = String.format("%d min, %d sec",
            TimeUnit.MILLISECONDS.toMinutes(timeElapsed),
            TimeUnit.MILLISECONDS.toSeconds(timeElapsed) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeElapsed))
        );
        guiFrame.finalScreen(readableTime);
    }

    public void showErrorScreen(String error) {
        guiFrame.showErrorScreen(error);
    }
}