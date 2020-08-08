package pl.edu.mimuw.dbaugmentor.messages;

public class ValidationResponse implements Message {
    private final ValidationStatus status;
    private String specificMsg;

    public ValidationResponse(ValidationStatus status) {
        this.status = status;
    }

    public ValidationResponse(ValidationStatus status, String specificMsg) {
        this.status = status;
        this.specificMsg = specificMsg;
    }

    public String getErrorMsg() {
        if (specificMsg != null && !specificMsg.equals("")) {
            return specificMsg;
        }
        return status.toString();
    }

    public boolean isSuccess() {
         return status == ValidationStatus.OK;
    }

    @Override
    public String getTextOfMessage() {
        if (isSuccess()) {
            return "Data is correct";
        }
        return getErrorMsg();
    }
}
