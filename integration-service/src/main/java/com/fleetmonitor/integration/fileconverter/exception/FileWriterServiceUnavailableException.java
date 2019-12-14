package com.fleetmonitor.integration.fileconverter.exception;

/**
 * Created by Shahrooz on 12/12/2019.
 */
public class FileWriterServiceUnavailableException extends RuntimeException {

    public FileWriterServiceUnavailableException(String message) {
        super(message);
    }

    public FileWriterServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
