package com.fleetmonitor.integration.fileconverter.exception;

/**
 * Created by Shahrooz on 12/12/2019.
 */
public class FileConverterServiceUnavailableException extends RuntimeException {

    public FileConverterServiceUnavailableException(String message) {
        super(message);
    }

    public FileConverterServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
