package com.fleetmonitor.integration.fileconverter;

import org.springframework.web.multipart.MultipartFile;

/**
 * A Service for File conversion.
 * Created by Shahrooz on 12/12/2019.
 */
public interface FileConverterService {

    /**
     * <p>Validate and Convert the input file to XML.
     * <p>For its validation will call {@link FileValidatorService}.
     * <p>Will use a StopWatch to estimate the time each validation take and also how long it takes to convert the file to xml.
     *
     * @param file
     * @return Converted XML String
     */
    String convertFile(MultipartFile file);
}
