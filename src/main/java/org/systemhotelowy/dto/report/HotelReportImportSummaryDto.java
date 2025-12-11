package org.systemhotelowy.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelReportImportSummaryDto {

    private String date;
    private int roomsCount;
    private int tasksCount;
    private int reservationsCount;
}
