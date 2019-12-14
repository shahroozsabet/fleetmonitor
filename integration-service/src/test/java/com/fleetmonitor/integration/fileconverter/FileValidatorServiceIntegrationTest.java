package com.fleetmonitor.integration.fileconverter;

import com.fleetmonitor.integration.fileconverter.exception.FileErrorException;
import com.fleetmonitor.integration.fileconverter.filewriter.FileWriterService;
import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by shsabet on 12/14/2019.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
class FileValidatorServiceIntegrationTest {

    private FileValidatorService fileValidatorService;

    @Autowired
    private MessageSource messageSource;
    @MockBean
    private Tika tika;
    @MockBean
    private FileWriterService fileWriterService;

    @BeforeEach
    void setUp() {
        fileValidatorService = new FileValidatorServiceImpl(messageSource, tika, fileWriterService);
    }

    @Test
    void validateNullFile() {
        assertThrows(FileErrorException.class, () -> fileValidatorService.validateFile(null));
    }

    @Test
    void validateEmptyFile() throws IOException {
        ClassPathResource resource = new ClassPathResource("sampleCSVErr4.csv", getClass());
        MockMultipartFile multipartFile = new MockMultipartFile("file", resource.getInputStream());
        FileErrorException fileErrorException = assertThrows(FileErrorException.class, () -> fileValidatorService.validateFile(multipartFile));
        assertTrue(fileErrorException.getFileErrorDTO().getGeneralErrors().contains("File is Empty ."));
    }

}