package pl.edu.mimuw.dbaugmentor.messages;

public class ValidationRequest implements Message {
    @Override
    public String getTextOfMessage() {
        return "Validation request";
    }
}
