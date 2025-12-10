package org.systemhotelowy.unit.report;

import org.junit.jupiter.api.Test;
import org.systemhotelowy.service.impl.XmlReportExportService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class XmlReportExportServiceTest {

    @Test
    void exportDailyReport_shouldGenerateXmlWithCorrectRootAndDate() {
        // given
        XmlReportExportService service = new XmlReportExportService(null, null);
        LocalDate date = LocalDate.of(2025, 12, 9);

        // when
        byte[] xmlBytes = service.exportDailyReport(date);
        String xml = new String(xmlBytes, StandardCharsets.UTF_8);

        // then
        assertThat(xml)
                .contains("<hotelReport")
                .contains("date=\"2025-12-09\"");
    }

    @Test
    void exportDailyReport_shouldIncludeEmptySectionsWhenNoData() {
        // given
        XmlReportExportService service = new XmlReportExportService(null, null);
        LocalDate date = LocalDate.of(2025, 12, 9);

        // when
        byte[] xmlBytes = service.exportDailyReport(date);
        String xml = new String(xmlBytes, StandardCharsets.UTF_8);

        // then
        assertThat(xml)
                .contains("<rooms")
                .contains("<tasks")
                .contains("<reservations");
    }
}
