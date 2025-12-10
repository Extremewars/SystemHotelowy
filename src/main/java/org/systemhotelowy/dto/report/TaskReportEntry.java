package org.systemhotelowy.dto.report;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskReportEntry {

    @XmlAttribute
    private Integer id;

    @XmlAttribute
    private String status;

    @XmlAttribute
    private String roomNumber;

    @XmlAttribute
    private String assignedToEmail;

    @XmlAttribute
    private String scheduledDate; // yyyy-MM-dd

    @XmlAttribute
    private Integer durationInMinutes;
}
