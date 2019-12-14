package com.fleetmonitor.integration.fileconverter.exception;

/**
 * Created by Shahrooz on 12/12/2019.
 */
public class CSVException extends RuntimeException {
    private byte[] content;

    public CSVException(String message, byte[] content) {
        super(message);
        this.content = content;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
