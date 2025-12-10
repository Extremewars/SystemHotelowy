package org.systemhotelowy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.systemhotelowy.dto.report.HotelReportDto;
import org.systemhotelowy.service.ReportExportService;
import org.systemhotelowy.service.impl.XmlReportExportService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Endpoints for hotel reports (XML export/import)")
public class ReportController {

    private final ReportExportService reportExportService;

    @GetMapping(
            value = "/daily/xml",
            produces = MediaType.APPLICATION_XML_VALUE
    )
    @Operation(
            summary = "Get daily hotel report as XML",
            description = "Returns XML report with rooms, tasks and reservations for given date."
    )
    public ResponseEntity<byte[]> getDailyXmlReport(
            @RequestParam("date")
            @DateTimeFormat(iso = DATE)
            @Parameter(description = "Report date in format yyyy-MM-dd", example = "2025-12-10")
            LocalDate date
    ) {
        byte[] xmlBytes = reportExportService.exportDailyReport(date);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("hotel-report-" + date + ".xml")
                        .build()
        );

        return new ResponseEntity<>(xmlBytes, headers, HttpStatus.OK);
    }

    @PostMapping(
            value = "/daily/xml/import",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Import daily hotel report from XML",
            description = "Accepts XML report (validated against XSD), deserializes it to DTO and returns summary (date + counts)."
    )
    public ResponseEntity<Map<String, Object>> importDailyXmlReport(
            @RequestBody byte[] xmlBytes
    ) {

        if (!(reportExportService instanceof XmlReportExportService xmlService)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        HotelReportDto dto = xmlService.importDailyReport(xmlBytes);

        Map<String, Object> summary = new HashMap<>();
        summary.put("date", dto.getDate());
        summary.put("roomsCount", dto.getRooms().size());
        summary.put("tasksCount", dto.getTasks().size());
        summary.put("reservationsCount", dto.getReservations().size());

        return ResponseEntity.ok(summary);
    }
}



