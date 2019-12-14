package com.fleetmonitor.integration.fileconverter.filewriter;

/**
 * A service for converting file content to another types.
 * Created by Shahrooz on 12/12/2019.
 */
public interface FileWriterService {


    /**
     * will get the content of a file as text and convert it to byte[]
     *
     * @param text
     * @return Content of the String as a byte[]
     */
    byte[] toByteArray(String text);

}
