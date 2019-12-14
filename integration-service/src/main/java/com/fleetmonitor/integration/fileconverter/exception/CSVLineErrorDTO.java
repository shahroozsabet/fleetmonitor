package com.fleetmonitor.integration.fileconverter.exception;

import com.fleetmonitor.integration.util.CommonEnum;
import lombok.Builder;
import lombok.Data;

/**
 * This DTO will represent each line of the returned CSV error file after file validation occurred in backend.
 * Created by Shahrooz on 12/12/2019.
 */
@Data
@Builder
public class CSVLineErrorDTO {
    private String fileName;
    private Integer lineNo;
    private String fieldName;
    private String fieldValue;
    private String errorDescription;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(fileName).append(CommonEnum.CSVDelimiter.PIPE.getValue());
        sb.append(lineNo).append(CommonEnum.CSVDelimiter.PIPE.getValue());
        sb.append(fieldName).append(CommonEnum.CSVDelimiter.PIPE.getValue());
        sb.append(fieldValue).append(CommonEnum.CSVDelimiter.PIPE.getValue());
        sb.append(errorDescription).append(System.getProperty("line.separator"));
        return sb.toString();
    }
}
