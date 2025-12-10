package org.systemhotelowy.service;

import java.time.LocalDate;

public interface ReportExportService {

    byte[] exportDailyReport(LocalDate date);
}
