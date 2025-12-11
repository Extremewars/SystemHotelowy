package org.systemhotelowy.integration.report;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // -------------------------------------------
    // 1. TEST GET — XML EXPORT
    // -------------------------------------------
    @Test
    void getDailyXmlReport_shouldReturnXmlWithHotelReportRoot() throws Exception {
        LocalDate date = LocalDate.of(2025, 12, 9);

        mockMvc.perform(
                        get("/api/reports/daily/xml")
                                .param("date", "2025-12-09")
                                .accept(MediaType.APPLICATION_XML)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(content().string(containsString("<hotelReport")));
    }

    // -------------------------------------------
    // 2. TEST POST — poprawny XML (import)
    // -------------------------------------------
    @Test
    void importDailyXml_shouldReturnValidSummary() throws Exception {

        String xmlContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <hotelReport date="2025-12-09">
                    <rooms>
                        <room id="1" number="911" floor="9" status="READY" type="SINGLE"/>
                        <room id="2" number="101" floor="10" status="DIRTY" type="DOUBLE"/>
                    </rooms>
                    <tasks>
                        <task id="99"
                              status="PENDING"
                              roomNumber="911"
                              assignedToEmail="test@hotel.com"
                              scheduledDate="2025-12-09"
                              durationInMinutes="30"/>
                    </tasks>
                    <reservations/>
                </hotelReport>
                """;

        mockMvc.perform(
                        post("/api/reports/daily/xml/import")
                                .contentType(MediaType.APPLICATION_XML)
                                .content(xmlContent)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.date").value("2025-12-09"))
                .andExpect(jsonPath("$.roomsCount").value(2))
                .andExpect(jsonPath("$.tasksCount").value(1))
                .andExpect(jsonPath("$.reservationsCount").value(0));
    }

    // -------------------------------------------
    // 3. TEST POST — niepoprawny XML (błąd XSD)
    // -------------------------------------------
    @Test
    void importDailyXml_shouldReturnBadRequestForInvalidXml() throws Exception {

        // Ten XML celowo nie pasuje do schematu xsd/hotel-report.xsd
        String invalidXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <hotelReportt date="2025-12-09">
                    <invalidRoot/>
                </hotelReportt>
                """;

        mockMvc.perform(
                        post("/api/reports/daily/xml/import")
                                .contentType(MediaType.APPLICATION_XML)
                                .content(invalidXml)
                )
                .andExpect(status().isBadRequest());
    }
}
