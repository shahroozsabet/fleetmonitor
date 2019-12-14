package com.fleetmonitor.integration.fileconverter.exception;

import com.fleetmonitor.integration.fileconverter.dto.FileErrorDTO;

/**
 * Created by Shahrooz on 12/12/2019.
 */
public class FileErrorException extends RuntimeException {

    private FileErrorDTO fileErrorDTO;

    public FileErrorException(FileErrorDTO fileErrorDTO) {
        super(fileErrorDTO.toString());
        this.fileErrorDTO = fileErrorDTO;
    }

    public FileErrorDTO getFileErrorDTO() {
        return fileErrorDTO;
    }

    public void setFileErrorDTO(FileErrorDTO fileErrorDTO) {
        this.fileErrorDTO = fileErrorDTO;
    }
}
