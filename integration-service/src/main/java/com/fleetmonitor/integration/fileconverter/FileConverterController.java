package com.fleetmonitor.integration.fileconverter;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Shahrooz on 12/12/2019.
 */
@RestController
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FileConverterController {

    private final FileConverterService fileConverterService;

    /**
     * {@linkplain HttpStatus#CREATED CREATED} for converted XML, or {@linkplain HttpStatus#BAD_REQUEST BAD_REQUEST} with a general error String Json,
     * Or a {@linkplain HttpStatus#BAD_REQUEST BAD_REQUEST} for a return CSV file error.
     *
     * @param file a Pipe separated file
     * @return The Converted XML File
     */
    @PostMapping(value = "/convertFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> convertFile(@RequestParam(name = "file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.TEXT_XML).body(fileConverterService.convertFile(file));
    }

}
