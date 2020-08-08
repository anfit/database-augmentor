package pl.edu.mimuw.dbaugmentor;

import pl.edu.mimuw.dbaugmentor.gui.GUI;
import pl.edu.mimuw.dbaugmentor.messages.CopyRequest;
import pl.edu.mimuw.dbaugmentor.messages.ErrorMsg;
import pl.edu.mimuw.dbaugmentor.messages.Message;
import pl.edu.mimuw.dbaugmentor.messages.StatusMsg;

import java.util.Queue;

public class GuiThread extends Thread {
    private final GUI gui;
    private final Queue<Message> msgQueue;

    GuiThread(GUI gui, Queue<Message> msgQueue) {
        this.gui = gui;
        this.msgQueue = msgQueue;
    }

    @Override
    public void run() {
        synchronized (gui) {
            try {
                gui.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        gui.loadingScreen();
        synchronized (msgQueue) {
            msgQueue.add(new CopyRequest());
            msgQueue.notify();
        }
        long startTime = System.currentTimeMillis();
        boolean copyingInProgress = true;

        while (copyingInProgress) {
            synchronized (msgQueue) {
                try {
                    msgQueue.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (msgQueue.isEmpty()) {
                    copyingInProgress = false;
                } else {
                    Message message = msgQueue.poll();
                    if (message instanceof StatusMsg) {
                        gui.updateLoadingScreen(((StatusMsg) message).getStatus());
                    }
                    if (message instanceof ErrorMsg) {
                        gui.showErrorScreen(((ErrorMsg) message).getError());
                        return;
                    }
                }
            }
        }
        long endTime = System.currentTimeMillis();
        gui.finalScreen(endTime - startTime);
    }
}
