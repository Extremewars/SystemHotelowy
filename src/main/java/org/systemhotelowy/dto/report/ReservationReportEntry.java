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
public class ReservationReportEntry {

    @XmlAttribute
    private Integer id;

    @XmlAttribute
    private String roomNumber;

    @XmlAttribute
    private String status;

    @XmlAttribute
    private String guestName;

    @XmlAttribute
    private String checkInDate; // yyyy-MM-dd

    @XmlAttribute
    private String checkOutDate; // yyyy-MM-dd
}
