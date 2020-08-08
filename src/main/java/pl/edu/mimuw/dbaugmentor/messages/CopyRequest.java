package pl.edu.mimuw.dbaugmentor.messages;

public class CopyRequest implements Message {
    @Override
    public String getTextOfMessage() {
        return "Copy request";
    }
}
