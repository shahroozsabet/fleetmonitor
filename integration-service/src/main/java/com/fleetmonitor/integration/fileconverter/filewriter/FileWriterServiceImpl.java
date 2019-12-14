package com.fleetmonitor.integration.fileconverter.filewriter;

import com.fleetmonitor.integration.fileconverter.exception.FileWriterServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Created by Shahrooz on 12/12/2019.
 */
@Component
@Slf4j
public class FileWriterServiceImpl implements FileWriterService {

    private final MessageSource messageSource;

    public FileWriterServiceImpl(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public byte[] toByteArray(String text) {
        if (StringUtils.isBlank(text)) return null;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byteArrayOutputStream.write(text.getBytes(StandardCharsets.UTF_8));
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("MSG_200317", e);
            throw new FileWriterServiceUnavailableException(messageSource.getMessage("MSG_200317", null, new Locale("fa")), e);
        }
    }

}
