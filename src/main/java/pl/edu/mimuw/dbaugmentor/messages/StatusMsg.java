package pl.edu.mimuw.dbaugmentor.messages;

public class StatusMsg implements Message {
    private final Status status;

    public StatusMsg(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String getTextOfMessage() {
        return getStatus().toString() + " - done";
    }
}
