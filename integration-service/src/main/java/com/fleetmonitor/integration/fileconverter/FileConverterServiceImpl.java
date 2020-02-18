package com.fleetmonitor.integration.fileconverter;

import com.fleetmonitor.integration.fileconverter.dto.*;
import com.fleetmonitor.integration.fileconverter.exception.FileConverterServiceUnavailableException;
import com.fleetmonitor.integration.util.CommonEnum;
import com.fleetmonitor.integration.xml.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * Created by Shahrooz on 12/12/2019.
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FileConverterServiceImpl implements FileConverterService {

    private final FileValidatorService fileValidatorService;
    private final MessageSource messageSource;

    @Override
    public String convertFile(MultipartFile file) {
        StopWatch stopWatch = new StopWatch("convertFile");
        stopWatch.start("validateFile");
        fileValidatorService.validateFile(file);
        stopWatch.stop();
        stopWatch.start("convertToXML");
        String xmlStr = convertToXML(file);
        stopWatch.stop();
        log.info("file with size={} converted, running time (s) = {}", file.getSize(), stopWatch.getTotalTimeSeconds());
        log.info("{}", stopWatch.prettyPrint());
        return xmlStr;
    }

    /**
     * <p>Will convert input file and return its XML representation.
     * <p>It will call a helper enum {@link ObjectToXml#convertToXML(People)} in commons which use JAXB2 for converting to xml.
     *
     * @param file
     * @return String representation of converted XML file
     * @throws FileConverterServiceUnavailableException in case of underlying service failed MSG_200106.
     */
    private String convertToXML(MultipartFile file) {
        List<DataEntry> dataEntries = new ArrayList<>();
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try (InputStream inputStream = file.getInputStream()) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    AtomicReference<DataEntry> dataEntry = new AtomicReference<>(new DataEntry());
                    bufferedReader.lines().filter(line -> !line.trim().isEmpty()).map(line -> line.trim().split(Pattern.quote(CommonEnum.CSVDelimiter.PIPE.getValue()))).forEach(columns -> {
                        if (CSVType.P.name().equals(columns[0])) {
                            dataEntry.set(new DataEntry());
                            dataEntry.get().setFirstName(columns[CSVFieldP.FIRST_NAME.ordinal()]);
                            dataEntry.get().setLastName(columns[CSVFieldP.LAST_NAME.ordinal()]);
                            dataEntries.add(dataEntry.get());
                        } else if (CSVType.T.name().equals(columns[0])) {
                            if (dataEntry.get().getFamilies().isEmpty()) {
                                dataEntry.get().setMobile(columns[CSVFieldT.MOBILE.ordinal()]);
                                dataEntry.get().setLandPhone(columns[CSVFieldT.PHONE.ordinal()]);
                            } else {
                                Phone phone = new Phone();
                                phone.setMobile(columns[CSVFieldT.MOBILE.ordinal()]);
                                phone.setLandPhone(columns[CSVFieldT.PHONE.ordinal()]);
                                dataEntry.get().getFamilies().peek().setPhone(phone);
                            }
                        } else if (CSVType.A.name().equals(columns[0])) {
                            if (dataEntry.get().getFamilies().isEmpty()) {
                                dataEntry.get().setStreet(columns[CSVFieldA.ADDRESS.ordinal()]);
                                dataEntry.get().setTown(columns[CSVFieldA.TOWN.ordinal()]);
                                dataEntry.get().setPostalCode(columns[CSVFieldA.POSTAL_CODE.ordinal()]);
                            } else {
                                Address address = new Address();
                                address.setTown(columns[CSVFieldA.TOWN.ordinal()]);
                                address.setStreet(columns[CSVFieldA.ADDRESS.ordinal()]);
                                address.setPostalCode(columns[CSVFieldA.POSTAL_CODE.ordinal()]);
                                dataEntry.get().getFamilies().peek().setAddress(address);
                            }
                        } else {
                            Family family = new Family();
                            family.setName(columns[CSVFieldF.NAME.ordinal()]);
                            family.setBorn(columns[CSVFieldF.YEAR.ordinal()]);
                            dataEntry.get().getFamilies().add(family);
                        }
                    });
                }
            }
            return ObjectToXml.INSTANCE.convertToXML(ObjectToXml.INSTANCE.createPeopleByEntries(dataEntries));
        } catch (IOException | JAXBException e) {
            log.error("MSG_200106", e);
            throw new FileConverterServiceUnavailableException(messageSource.getMessage("MSG_200106", new String[]{fileName}, new Locale("en")), e);
        }
    }

}
