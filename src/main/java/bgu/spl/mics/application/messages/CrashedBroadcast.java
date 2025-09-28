package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class CrashedBroadcast implements Broadcast {
    private final String source; // which object caused the error
    private final String errorMessage; // error message

    public CrashedBroadcast(String source, String errorMessage) {
        this.source = source;
        this.errorMessage = errorMessage;
    }

    public String getSource() {
        return source;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getServiceName() {
        return source;
    }
}
