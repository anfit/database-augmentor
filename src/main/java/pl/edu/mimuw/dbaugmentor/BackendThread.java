package pl.edu.mimuw.dbaugmentor;

import pl.edu.mimuw.dbaugmentor.messages.CopyRequest;
import pl.edu.mimuw.dbaugmentor.messages.Message;

import java.util.Queue;

public class BackendThread extends Thread {
    private final Backend backend;
    private final Queue<Message> msgQueue;

    public BackendThread(Backend backend, Queue<Message> msgQueue) {
        this.backend = backend;
        this.msgQueue = msgQueue;
    }

    @Override
    public void run() {
        boolean copyingInProgress = false;
        while (!copyingInProgress) {
            synchronized (msgQueue) {
                try {
                    msgQueue.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Message msg = msgQueue.poll();
                if (msg instanceof CopyRequest) {
                    copyingInProgress = true;
                } else {
                    backend.runValidationWithGui();
                }
            }
        }
        backend.runCopyProcess();
    }
}
