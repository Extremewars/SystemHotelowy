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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getDailyXmlReport_shouldReturnXmlWithHotelReportRoot() throws Exception {
        // given
        LocalDate date = LocalDate.of(2025, 12, 9);

        // when + then
        mockMvc.perform(
                        get("/api/reports/daily/xml")
                                .param("date", "2025-12-09")
                                .accept(MediaType.APPLICATION_XML)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(content().string(containsString("<hotelReport")));
    }
}
