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
public class RoomReportEntry {

    @XmlAttribute
    private Integer id;

    @XmlAttribute
    private String number;

    @XmlAttribute
    private Integer floor;

    @XmlAttribute
    private String status;

    @XmlAttribute
    private String type;
}
