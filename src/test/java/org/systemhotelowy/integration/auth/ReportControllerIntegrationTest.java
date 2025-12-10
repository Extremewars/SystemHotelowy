@Test
void importDailyXml_shouldReturnValidSummary() throws Exception {
    String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <hotelReport date="2025-12-10">
                <rooms/>
                <tasks/>
                <reservations/>
            </hotelReport>
            """;

    mockMvc.perform(post("/api/reports/daily/xml/import")
                    .contentType(MediaType.APPLICATION_XML)
                    .content(xml))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.date").value("2025-12-10"))
            .andExpect(jsonPath("$.roomsCount").value(0))
            .andExpect(jsonPath("$.tasksCount").value(0))
            .andExpect(jsonPath("$.reservationsCount").value(0));
}
