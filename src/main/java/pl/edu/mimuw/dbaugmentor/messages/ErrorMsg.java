package pl.edu.mimuw.dbaugmentor.messages;

public class ErrorMsg implements Message {
    private final String error;

    public ErrorMsg(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    @Override
    public String getTextOfMessage() {
        return getError();
    }
}
