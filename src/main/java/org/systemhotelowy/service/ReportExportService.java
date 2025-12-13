package org.systemhotelowy.service;

import org.systemhotelowy.dto.report.HotelReportImportSummaryDto;

import java.time.LocalDate;

public interface ReportExportService {

    byte[] exportDailyReport(LocalDate date);

    HotelReportImportSummaryDto importDailyReport(byte[] xmlBytes);
}
