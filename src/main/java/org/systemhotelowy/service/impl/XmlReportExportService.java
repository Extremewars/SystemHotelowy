package org.systemhotelowy.service.impl;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.systemhotelowy.dto.report.HotelReportDto;
import org.systemhotelowy.dto.report.HotelReportImportSummaryDto;
import org.systemhotelowy.dto.report.RoomReportEntry;
import org.systemhotelowy.dto.report.TaskReportEntry;
import org.systemhotelowy.model.Room;
import org.systemhotelowy.model.RoomStatus;
import org.systemhotelowy.model.Task;
import org.systemhotelowy.model.TaskStatus;
import org.systemhotelowy.service.ReportExportService;
import org.systemhotelowy.service.RoomService;
import org.systemhotelowy.service.TaskService;
import org.systemhotelowy.service.UserService;

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
    private final UserService userService;

    /**
     * Eksport dziennego raportu hotelowego do XML (używany przez GET /api/reports/daily/xml).
     */
    @Override
    public byte[] exportDailyReport(LocalDate date) {
        HotelReportDto reportDto = buildReport(date);
        return marshalToXml(reportDto);
    }

    /**
     * Import dziennego raportu hotelowego z XML (używany przez POST /api/reports/daily/xml/import).
     * Waliduje XML względem XSD, deserializuje do HotelReportDto, następnie zwraca podsumowanie.
     */
    public HotelReportImportSummaryDto importDailyReport(byte[] xmlBytes) {
        try {
            // 1. Walidacja względem XSD
            validateAgainstSchema(xmlBytes);

            // 2. Deserializacja XML -> HotelReportDto
            JAXBContext context = JAXBContext.newInstance(HotelReportDto.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            HotelReportDto dto = (HotelReportDto)
                    unmarshaller.unmarshal(new ByteArrayInputStream(xmlBytes));

            // 2a. Przetworzenie danych (aktualizacja bazy)
            processImportedData(dto);

            // 3. Zbudowanie podsumowania
            HotelReportImportSummaryDto summary = new HotelReportImportSummaryDto();
            summary.setDate(dto.getDate());
            summary.setRoomsCount(dto.getRooms() != null ? dto.getRooms().size() : 0);
            summary.setTasksCount(dto.getTasks() != null ? dto.getTasks().size() : 0);
            summary.setReservationsCount(dto.getReservations() != null ? dto.getReservations().size() : 0);

            return summary;
        } catch (Exception e) {
            log.error("Failed to import XML daily report", e);
            throw new IllegalArgumentException("Could not import XML daily report", e);
        }
    }

    /**
     * Aktualizuje stan bazy danych na podstawie zaimportowanego raportu.
     */
    private void processImportedData(HotelReportDto dto) {
        // 1. Aktualizacja pokoi
        if (dto.getRooms() != null) {
            for (RoomReportEntry roomEntry : dto.getRooms()) {
                if (roomEntry.getId() != null) {
                    roomService.findById(roomEntry.getId()).ifPresent(room -> {
                        boolean changed = false;
                        // Aktualizacja statusu
                        if (roomEntry.getStatus() != null) {
                            try {
                                RoomStatus newStatus = RoomStatus.valueOf(roomEntry.getStatus());
                                if (room.getRoomStatus() != newStatus) {
                                    room.setRoomStatus(newStatus);
                                    changed = true;
                                }
                            } catch (IllegalArgumentException e) {
                                log.warn("Invalid room status in import: {}", roomEntry.getStatus());
                            }
                        }
                        
                        if (changed) {
                            roomService.update(room);
                        }
                    });
                }
            }
        }

        // 2. Aktualizacja zadań
        if (dto.getTasks() != null) {
            for (TaskReportEntry taskEntry : dto.getTasks()) {
                if (taskEntry.getId() != null) {
                    taskService.findById(taskEntry.getId()).ifPresent(task -> {
                        boolean changed = false;
                        // Aktualizacja statusu
                        if (taskEntry.getStatus() != null) {
                            try {
                                TaskStatus newStatus = TaskStatus.valueOf(taskEntry.getStatus());
                                if (task.getStatus() != newStatus) {
                                    task.setStatus(newStatus);
                                    changed = true;
                                }
                            } catch (IllegalArgumentException e) {
                                log.warn("Invalid task status in import: {}", taskEntry.getStatus());
                            }
                        }

                        // Aktualizacja przypisanego użytkownika (opcjonalnie)
                        if (taskEntry.getAssignedToEmail() != null) {
                            userService.findByEmail(taskEntry.getAssignedToEmail()).ifPresent(user -> {
                                if (!user.equals(task.getAssignedTo())) {
                                    task.setAssignedTo(user);
                                    // changed = true; // Zmienna lokalna w lambdzie musi być finalna lub effectively final
                                    // W tym przypadku musimy wymusić update, jeśli zmieniliśmy usera
                                    taskService.update(task); 
                                }
                            });
                        }

                        if (changed) {
                            taskService.update(task);
                        }
                    });
                }
            }
        }
    }

    /**
     * Buduje DTO raportu na podstawie danych z serwisów domenowych.
     */
    private HotelReportDto buildReport(LocalDate date) {
        HotelReportDto dto = new HotelReportDto();
        dto.setDate(DATE_FORMATTER.format(date));

        // rooms
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

        // tasks
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

        // reservations – na razie puste
        return dto;
    }

    /**
     * Serializacja DTO -> XML + walidacja względem XSD.
     */
    private byte[] marshalToXml(HotelReportDto reportDto) {
        try {
            JAXBContext context = JAXBContext.newInstance(HotelReportDto.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            marshaller.marshal(reportDto, out);
            byte[] xmlBytes = out.toByteArray();

            // Walidacja wygenerowanego XML względem XSD
            validateAgainstSchema(xmlBytes);

            return xmlBytes;
        } catch (JAXBException e) {
            log.error("Error while marshalling hotel report to XML", e);
            throw new IllegalStateException("Could not generate XML report", e);
        }
    }

    /**
     * Walidacja XML względem schematu XSD (resources/xsd/hotel-report.xsd).
     */
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
