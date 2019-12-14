package com.fleetmonitor.integration.fileconverter;

import org.springframework.web.multipart.MultipartFile;

/**
 * A Service for validating file
 * Created by Shahrooz on 12/12/2019.
 */
public interface FileValidatorService {

    /**
     * <p>Will validate file and throws different exception message if it doesnt pass the needed perquisites.
     * <p>Will use a StopWatch to estimate the time each validation take.
     *
     * @param file
     */
    void validateFile(MultipartFile file);

}
