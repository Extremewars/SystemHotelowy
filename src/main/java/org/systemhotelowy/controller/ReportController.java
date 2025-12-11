package org.systemhotelowy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.systemhotelowy.dto.report.HotelReportImportSummaryDto;
import org.systemhotelowy.service.ReportExportService;
import org.systemhotelowy.service.impl.XmlReportExportService;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Endpoints for hotel reports (XML export & import)")
public class ReportController {

    private final ReportExportService reportExportService;
    private final XmlReportExportService xmlService;

    // -------------------------------------------------
    // GET /api/reports/daily/xml  – EXPORT XML
    // -------------------------------------------------
    @GetMapping(
            value = "/daily/xml",
            produces = MediaType.APPLICATION_XML_VALUE
    )
    @Operation(
            summary = "Get daily hotel report as XML",
            description = "Returns XML report with rooms, tasks and reservations for given date.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "XML Report",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = MediaType.APPLICATION_XML_VALUE,
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                                            type = "string",
                                            format = "binary"
                                    )
                            )
                    )
            }
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

    // -------------------------------------------------
    // POST /api/reports/daily/xml/import – IMPORT XML
    // -------------------------------------------------
    @PostMapping(
            value = "/daily/xml/import",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Import daily hotel report from XML",
            description = "Validates XML against XSD schema and returns summary of imported data.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "XML file content",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = MediaType.APPLICATION_XML_VALUE,
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    type = "string",
                                    format = "binary"
                            )
                    )
            )
    )
    public ResponseEntity<?> importDailyXml(
            @RequestBody byte[] xmlBytes
    ) {
        HotelReportImportSummaryDto dto = xmlService.importDailyReport(xmlBytes);
        return ResponseEntity.ok(dto);
    }
}
