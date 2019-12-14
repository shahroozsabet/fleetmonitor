package com.fleetmonitor.integration.fileconverter;

import com.fleetmonitor.integration.fileconverter.dto.*;
import com.fleetmonitor.integration.fileconverter.exception.CSVException;
import com.fleetmonitor.integration.fileconverter.exception.CSVLineErrorDTO;
import com.fleetmonitor.integration.fileconverter.exception.FileConverterServiceUnavailableException;
import com.fleetmonitor.integration.fileconverter.exception.FileErrorException;
import com.fleetmonitor.integration.fileconverter.filewriter.FileWriterService;
import com.fleetmonitor.integration.util.CommonEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.tika.Tika;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Shahrooz on 12/12/2019.
 */
@Slf4j
@Component
public class FileValidatorServiceImpl implements FileValidatorService {

    private final MessageSource messageSource;
    private final Tika tika;
    private final FileWriterService fileWriterService;

    public FileValidatorServiceImpl(MessageSource messageSource, Tika tika, FileWriterService fileWriterService) {
        this.messageSource = messageSource;
        this.tika = tika;
        this.fileWriterService = fileWriterService;
    }

    @Override
    public void validateFile(MultipartFile file) {
        StopWatch stopWatch = new StopWatch("validateFile");
        stopWatch.start("emptyCheck");
        emptyCheck(file);
        stopWatch.stop();
        stopWatch.start("mimeTypeCheck");
        mimeTypeCheck(file);
        stopWatch.stop();
        stopWatch.start("checkFirstCharacter");
        checkFirstCharacter(file);
        stopWatch.stop();
        stopWatch.start("checkFirstCharacters");
        checkFirstCharacters(file);
        stopWatch.stop();
        stopWatch.start("fieldsOrderCheck");
        fieldsOrderCheck(file);
        stopWatch.stop();
        stopWatch.start("fieldsCheck");
        List<CSVLineErrorDTO> errors = fieldsCheck(file);
        stopWatch.stop();
        stopWatch.start("makeErrorCSVText");
        String errorFile = makeErrorCSVText(errors);
        stopWatch.stop();
        log.info("file with size={} validated, running time (s) = {}", file.getSize(), stopWatch.getTotalTimeSeconds());
        log.info("{}", stopWatch.prettyPrint());
        if (!errors.isEmpty())
            throw new CSVException(messageSource.getMessage("MSG_200319", null, new Locale("en")), fileWriterService.toByteArray(errorFile));
    }

    /**
     * If multipart file is empty will throw exception.
     *
     * @param file
     * @throws FileErrorException                       in case of File is empty or null, with message code MSG_200107
     * @throws FileConverterServiceUnavailableException in case of underlying service failed MSG_200313.
     */
    private void emptyCheck(MultipartFile file) {
        FileErrorDTO fileErrorDTO = new FileErrorDTO();
        if (file == null || file.isEmpty()) {
            fileErrorDTO.getGeneralErrors().add(messageSource.getMessage("MSG_200107", new String[]{""}, new Locale("en")));
            throw new FileErrorException(fileErrorDTO);
        }
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (file.isEmpty()) {
            fileErrorDTO.getGeneralErrors().add(messageSource.getMessage("MSG_200107", new String[]{fileName}, new Locale("en")));
            throw new FileErrorException(fileErrorDTO);
        }
        try (InputStream inputStream = file.getInputStream()) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    if (bufferedReader.lines().allMatch(line -> line.trim().isEmpty())) {
                        fileErrorDTO.getGeneralErrors().add(messageSource.getMessage("MSG_200107", new String[]{fileName}, new Locale("en")));
                        throw new FileErrorException(fileErrorDTO);
                    }
                }
            }
        } catch (IOException e) {
            log.error("MSG_200313", e);
            throw new FileConverterServiceUnavailableException(messageSource.getMessage("MSG_200313", new String[]{fileName}, new Locale("en")), e);
        }
    }

    /**
     * This method is using apache.tika core library to validate if the input file type is plain text or not.
     *
     * @param file
     * @throws FileErrorException                       in case of File content type is not {@link MimeTypeUtils#TEXT_PLAIN}, with message code MSG_200303
     * @throws FileConverterServiceUnavailableException in case of underlying service failed MSG_200321.
     */
    private void mimeTypeCheck(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            String detectedMimeType = tika.detect(file.getBytes());
            if (!MimeTypeUtils.TEXT_PLAIN_VALUE.equals(detectedMimeType)) {
                FileErrorDTO fileErrorDTO = new FileErrorDTO();
                fileErrorDTO.getGeneralErrors().add(messageSource.getMessage("MSG_200303", new String[]{fileName, CommonEnum.MimeType.TEXT_CSV.getValue(), detectedMimeType}, new Locale("en")));
                throw new FileErrorException(fileErrorDTO);
            }
        } catch (IOException e) {
            log.error("MSG_200321", e);
            throw new FileConverterServiceUnavailableException(messageSource.getMessage("MSG_200321", new String[]{fileName}, new Locale("en")), e);
        }
    }

    /**
     * First character of each line should be among {@link CSVType} , otherwise validation will be failed with MSG_200101.
     *
     * @param file
     * @throws FileErrorException                       in case of First char of each line is not in {@link CSVType}, with message code MSG_200101
     * @throws FileConverterServiceUnavailableException in case of underlying service failed MSG_200313.
     */
    private void checkFirstCharacters(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try (InputStream inputStream = file.getInputStream()) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    List<String> csvTypes = Arrays.stream(CSVType.values()).map(CSVType::name).collect(Collectors.toList());
                    if (bufferedReader.lines().filter(line -> !line.trim().isEmpty())
                            .map(line -> line.trim().split(Pattern.quote(CommonEnum.CSVDelimiter.PIPE.getValue())))
                            .map(strings -> strings[0])
                            .anyMatch(string -> !csvTypes.contains(string))) {
                        FileErrorDTO fileErrorDTO = new FileErrorDTO();
                        fileErrorDTO.getGeneralErrors().add(messageSource.getMessage("MSG_200101", new String[]{Arrays.toString(CSVType.values())}, new Locale("en")));
                        throw new FileErrorException(fileErrorDTO);
                    }
                }
            }
        } catch (IOException e) {
            log.error("MSG_200313", e);
            throw new FileConverterServiceUnavailableException(messageSource.getMessage("MSG_200313", new String[]{fileName}, new Locale("en")), e);
        }
    }

    /**
     * First character of the file should be equal to P.
     *
     * @param file
     * @throws FileErrorException                       in case of first char of the file is not P, with message code MSG_200100
     * @throws FileConverterServiceUnavailableException in case of underlying service failed MSG_200313.
     */
    private void checkFirstCharacter(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try (InputStream inputStream = file.getInputStream()) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    if (!CSVType.P.name().equals(bufferedReader.lines().filter(line -> !line.trim().isEmpty())
                            .map(line -> line.trim().split(Pattern.quote(CommonEnum.CSVDelimiter.PIPE.getValue())))
                            .map(strings -> strings[0]).findFirst().get())) {
                        FileErrorDTO fileErrorDTO = new FileErrorDTO();
                        fileErrorDTO.getGeneralErrors().add(messageSource.getMessage("MSG_200100", new String[]{Arrays.toString(CSVType.values())}, new Locale("en")));
                        throw new FileErrorException(fileErrorDTO);
                    }
                }
            }
        } catch (IOException e) {
            log.error("MSG_200313", e);
            throw new FileConverterServiceUnavailableException(messageSource.getMessage("MSG_200313", new String[]{fileName}, new Locale("en")), e);
        }
    }

    /**
     * P& F has followe character {@link CSVFollowerP}, {@link CSVFollowerF}
     *
     * @param file
     * @throws FileErrorException                       in case of follower character is wrong, with message code MSG_200102
     * @throws FileConverterServiceUnavailableException in case of underlying service failed MSG_200313.
     */
    private void fieldsOrderCheck(MultipartFile file) {
        FileErrorDTO fileErrorDTO = new FileErrorDTO();
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try (InputStream inputStream = file.getInputStream()) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    List<String> firstChars = bufferedReader.lines().filter(line -> !line.trim().isEmpty())
                            .map(line -> line.trim().split(Pattern.quote(CommonEnum.CSVDelimiter.PIPE.getValue())))
                            .map(strings -> strings[0]).collect(Collectors.toList());
                    List<String> followerP = Arrays.stream(CSVFollowerP.values()).map(CSVFollowerP::name).collect(Collectors.toList());
                    List<String> followerF = Arrays.stream(CSVFollowerF.values()).map(CSVFollowerF::name).collect(Collectors.toList());
                    IntStream.range(0, firstChars.size() - 1).forEach(i -> {
                        String firstChar = firstChars.get(i);
                        if (firstChar.equals(CSVType.P.name())) {
                            if (!followerP.contains(firstChars.get(i + 1))) {
                                fileErrorDTO.getGeneralErrors().add(messageSource.getMessage("MSG_200102", new String[]{CSVType.P.name(), Arrays.toString(CSVFollowerP.values())}, new Locale("en")));
                                throw new FileErrorException(fileErrorDTO);
                            }
                        }
                        if (firstChar.equals(CSVType.F.name())) {
                            if (!followerF.contains(firstChars.get(i + 1))) {
                                fileErrorDTO.getGeneralErrors().add(messageSource.getMessage("MSG_200102", new String[]{CSVType.F.name(), Arrays.toString(CSVFollowerF.values())}, new Locale("en")));
                                throw new FileErrorException(fileErrorDTO);
                            }
                        }
                    });
                }
            }
        } catch (IOException e) {
            log.error("MSG_200313", e);
            throw new FileConverterServiceUnavailableException(messageSource.getMessage("MSG_200313", new String[]{fileName}, new Locale("en")), e);
        }
    }

    /**
     * This method validate each column for every lines in the file, and return all the errors as a CSV error file.
     *
     * @param file
     * @return List<CSVLineErrorDTO> which will be a CSV file
     * @throws FileConverterServiceUnavailableException in case of underlying service failed MSG_200313.
     */
    private List<CSVLineErrorDTO> fieldsCheck(MultipartFile file) {
        List<CSVLineErrorDTO> errors = new ArrayList<>();
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try (InputStream inputStream = file.getInputStream()) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    AtomicInteger lineNo = new AtomicInteger(0);
                    bufferedReader.lines().filter(line -> !line.trim().isEmpty()).map(line -> line.trim().split(Pattern.quote(CommonEnum.CSVDelimiter.PIPE.getValue()))).forEach(columns -> {
                        lineNo.incrementAndGet();
                        if (CSVType.P.name().equals(columns[0])) {
                            if (CSVFieldP.values().length != columns.length)
                                errors.add(CSVLineErrorDTO.builder().lineNo(lineNo.get()).fileName(fileName).fieldValue(Arrays.toString(columns))
                                        .fieldName(CSVType.P.name())
                                        .errorDescription(messageSource.getMessage("MSG_200103", new Integer[]{CSVFieldP.values().length}, new Locale("en"))).build());
                            else {
                                if (!org.apache.commons.lang3.StringUtils.isAlphaSpace(columns[CSVFieldP.FIRST_NAME.ordinal()]) || columns[CSVFieldP.FIRST_NAME.ordinal()].length() < 2 || columns[CSVFieldP.FIRST_NAME.ordinal()].length() > 255) {
                                    errors.add(CSVLineErrorDTO.builder().lineNo(lineNo.get()).fileName(fileName).fieldValue(columns[CSVFieldP.FIRST_NAME.ordinal()])
                                            .fieldName(messageSource.getMessage("Field.firstName", null, new Locale("en")))
                                            .errorDescription(messageSource.getMessage("MSG_200308", null, new Locale("en"))).build());
                                }
                                if (!org.apache.commons.lang3.StringUtils.isAlphaSpace(columns[CSVFieldP.LAST_NAME.ordinal()]) || columns[CSVFieldP.LAST_NAME.ordinal()].length() < 2 || columns[CSVFieldP.LAST_NAME.ordinal()].length() > 255) {
                                    errors.add(CSVLineErrorDTO.builder().lineNo(lineNo.get()).fileName(fileName).fieldValue(columns[CSVFieldP.LAST_NAME.ordinal()])
                                            .fieldName(messageSource.getMessage("Field.lastName", null, new Locale("en")))
                                            .errorDescription(messageSource.getMessage("MSG_200308", null, new Locale("en"))).build());
                                }
                            }
                        } else if (CSVType.T.name().equals(columns[0])) {
                            if (CSVFieldT.values().length != columns.length)
                                errors.add(CSVLineErrorDTO.builder().lineNo(lineNo.get()).fileName(fileName).fieldValue(Arrays.toString(columns))
                                        .fieldName(CSVType.T.name())
                                        .errorDescription(messageSource.getMessage("MSG_200103", new Integer[]{CSVFieldP.values().length}, new Locale("en"))).build());
                            else {
                                if (!(Pattern.compile(CommonEnum.PhonePattern.MOBILE.getValue()).matcher(
                                        columns[CSVFieldT.MOBILE.ordinal()].trim()).matches() ||
                                        Pattern.compile(CommonEnum.PhonePattern.PHONE.getValue())
                                                .matcher(columns[CSVFieldT.MOBILE.ordinal()].trim()).matches())) {
                                    errors.add(CSVLineErrorDTO.builder().lineNo(lineNo.get()).fileName(fileName).fieldValue(columns[CSVFieldT.MOBILE.ordinal()])
                                            .fieldName(messageSource.getMessage("Field.mobile", null, new Locale("fa")))
                                            .errorDescription(messageSource.getMessage("MSG_200104", null, new Locale("fa"))).build());
                                }
                            }
                        } else if (CSVType.A.name().equals(columns[0])) {
                            if (CSVFieldA.values().length != columns.length)
                                errors.add(CSVLineErrorDTO.builder().lineNo(lineNo.get()).fileName(fileName).fieldValue(Arrays.toString(columns))
                                        .fieldName(CSVType.A.name())
                                        .errorDescription(messageSource.getMessage("MSG_200103", new Integer[]{CSVFieldP.values().length}, new Locale("en"))).build());
                        } else {
                            if (CSVFieldF.values().length != columns.length)
                                errors.add(CSVLineErrorDTO.builder().lineNo(lineNo.get()).fileName(fileName).fieldValue(Arrays.toString(columns))
                                        .fieldName(CSVType.F.name())
                                        .errorDescription(messageSource.getMessage("MSG_200103", new Integer[]{CSVFieldP.values().length}, new Locale("en"))).build());
                            else {
                                if (!org.apache.commons.lang3.StringUtils.isAlphaSpace(columns[CSVFieldF.NAME.ordinal()]) || columns[CSVFieldF.NAME.ordinal()].length() < 2 || columns[CSVFieldF.NAME.ordinal()].length() > 255) {
                                    errors.add(CSVLineErrorDTO.builder().lineNo(lineNo.get()).fileName(fileName).fieldValue(columns[CSVFieldF.NAME.ordinal()])
                                            .fieldName(messageSource.getMessage("Field.name", null, new Locale("en")))
                                            .errorDescription(messageSource.getMessage("MSG_200308", null, new Locale("en"))).build());
                                }
                                if (!NumberUtils.isDigits(columns[CSVFieldF.YEAR.ordinal()]) || columns[CSVFieldF.YEAR.ordinal()].length() != 4) {
                                    errors.add(CSVLineErrorDTO.builder().lineNo(lineNo.get()).fileName(fileName).fieldValue(columns[CSVFieldF.YEAR.ordinal()])
                                            .fieldName(messageSource.getMessage("Field.year", null, new Locale("fa")))
                                            .errorDescription(messageSource.getMessage("MSG_200105", new Integer[]{4}, new Locale("fa"))).build());
                                }
                            }
                        }
                    });
                }
            }
        } catch (IOException e) {
            log.error("MSG_200313", e);
            throw new FileConverterServiceUnavailableException(messageSource.getMessage("MSG_200313", new String[]{fileName}, new Locale("en")), e);
        }
        return errors;
    }

    /**
     * <p>This is a helper method to convert {@link CSVLineErrorDTO} to a string,
     * <p>to be used as error CSV content in frontend in case of validation is failed per lines of input file.
     *
     * @param errors The lines of error CSV file as {@link CSVLineErrorDTO}
     * @return The content of the error CSV file as a String
     */
    private String makeErrorCSVText(List<CSVLineErrorDTO> errors) {
        return errors.isEmpty() ? null :
                Arrays.stream(CommonEnum.RequestFileCSVError.values()).map(CommonEnum.RequestFileCSVError::getValue).collect(Collectors.joining(CommonEnum.CSVDelimiter.PIPE.getValue())) +
                        System.getProperty("line.separator") +
                        errors.stream().sorted(Comparator.comparing(CSVLineErrorDTO::getFileName).thenComparingInt(CSVLineErrorDTO::getLineNo)).map(CSVLineErrorDTO::toString).collect(Collectors.joining());
    }

}
