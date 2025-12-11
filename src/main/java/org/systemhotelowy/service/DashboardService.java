package org.systemhotelowy.service;

import org.systemhotelowy.dto.EmployeeKpiData;
import org.systemhotelowy.dto.ManagerKpiData;

public interface DashboardService {
    ManagerKpiData getManagerKpiData();

    EmployeeKpiData getEmployeeKpiData(Integer userId);
}
