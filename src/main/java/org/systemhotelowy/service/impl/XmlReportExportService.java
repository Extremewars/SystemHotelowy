package org.systemhotelowy.service.impl;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.systemhotelowy.dto.report.HotelReportDto;
import org.systemhotelowy.dto.report.RoomReportEntry;
import org.systemhotelowy.dto.report.TaskReportEntry;
import org.systemhotelowy.model.Room;
import org.systemhotelowy.model.Task;
import org.systemhotelowy.service.ReportExportService;
import org.systemhotelowy.service.RoomService;
import org.systemhotelowy.service.TaskService;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class XmlReportExportService implements ReportExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final RoomService roomService;
    private final TaskService taskService;

    @Override
    public byte[] exportDailyReport(LocalDate date) {
        HotelReportDto reportDto = buildReport(date);
        return marshalToXml(reportDto);
    }

    public HotelReportDto importDailyReport(byte[] xmlBytes) {
        try {
            validateAgainstSchema(xmlBytes);

            JAXBContext context = JAXBContext.newInstance(HotelReportDto.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            try (ByteArrayInputStream in = new ByteArrayInputStream(xmlBytes)) {
                Object result = unmarshaller.unmarshal(in);
                if (result instanceof HotelReportDto dto) {
                    return dto;
                }
                throw new IllegalStateException("Unexpected root element while unmarshalling hotel report XML");
            }
        } catch (Exception e) {
            log.error("Error while unmarshalling hotel report XML", e);
            throw new IllegalStateException("Could not import XML report", e);
        }
    }

    private HotelReportDto buildReport(LocalDate date) {
        HotelReportDto dto = new HotelReportDto();
        dto.setDate(DATE_FORMATTER.format(date));

        List<Room> rooms = roomService != null ? roomService.findAll() : List.of();
        rooms.stream()
                .map(room -> new RoomReportEntry(
                        room.getId(),
                        room.getNumber(),
                        room.getFloor(),
                        room.getRoomStatus() != null ? room.getRoomStatus().name() : null,
                        room.getType() != null ? room.getType().name() : null
                ))
                .forEach(dto.getRooms()::add);

        List<Task> tasks = taskService != null ? taskService.findByDate(date) : List.of();
        tasks.stream()
                .map(task -> new TaskReportEntry(
                        task.getId(),
                        task.getStatus() != null ? task.getStatus().name() : null,
                        task.getRoom() != null ? task.getRoom().getNumber() : null,
                        task.getAssignedTo() != null ? task.getAssignedTo().getEmail() : null,
                        task.getScheduledAt() != null
                                ? DATE_FORMATTER.format(task.getScheduledAt().toLocalDate())
                                : null,
                        task.getDurationInMinutes()
                ))
                .forEach(dto.getTasks()::add);

        return dto;
    }

    private byte[] marshalToXml(HotelReportDto reportDto) {
        try {
            JAXBContext context = JAXBContext.newInstance(HotelReportDto.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            marshaller.marshal(reportDto, out);
            byte[] xmlBytes = out.toByteArray();

            validateAgainstSchema(xmlBytes);

            return xmlBytes;
        } catch (JAXBException e) {
            log.error("Error while marshalling hotel report to XML", e);
            throw new IllegalStateException("Could not generate XML report", e);
        }
    }

    private void validateAgainstSchema(byte[] xmlBytes) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            try (var xsdStream = getClass().getClassLoader().getResourceAsStream("xsd/hotel-report.xsd")) {
                if (xsdStream == null) {
                    throw new IllegalStateException("XSD schema 'xsd/hotel-report.xsd' not found on classpath");
                }

                Schema schema = factory.newSchema(new StreamSource(xsdStream));
                Validator validator = schema.newValidator();
                validator.validate(new StreamSource(new ByteArrayInputStream(xmlBytes)));
            }
        } catch (Exception e) {
            log.error("XML report is not valid against hotel-report.xsd schema", e);
            throw new IllegalStateException("XML report is not valid against schema", e);
        }
    }
}
