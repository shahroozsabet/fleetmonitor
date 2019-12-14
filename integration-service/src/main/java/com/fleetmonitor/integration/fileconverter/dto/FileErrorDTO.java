package com.fleetmonitor.integration.fileconverter.dto;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Shahrooz on 12/12/2019.
 */
@Data
public class FileErrorDTO {
    private Set<String> generalErrors = new HashSet<>(0);
}
