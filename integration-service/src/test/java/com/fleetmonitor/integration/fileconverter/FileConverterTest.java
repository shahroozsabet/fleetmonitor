package com.fleetmonitor.integration.fileconverter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by shsabet on 12/14/2019.
 */
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
public class FileConverterTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private FileConverterService fileConverterService;

    @Test
    void shouldSaveUploadedFile() throws Exception {
        ClassPathResource resource = new ClassPathResource("sampleCSV1.csv", getClass());
        MockMultipartFile multipartFile = new MockMultipartFile("file", resource.getInputStream());
        this.mvc.perform(MockMvcRequestBuilders.multipart("/convertFile").file(multipartFile))
                .andExpect(status().isCreated());
        then(fileConverterService).should().convertFile(any(MultipartFile.class));
    }

}
