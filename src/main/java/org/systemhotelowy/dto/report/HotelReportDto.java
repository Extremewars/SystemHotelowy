package org.systemhotelowy.dto.report;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@XmlRootElement(name = "hotelReport")
@XmlAccessorType(XmlAccessType.FIELD)
public class HotelReportDto {

    @XmlAttribute
    private String date; // ISO-8601, np. 2025-12-10

    @XmlElementWrapper(name = "rooms")
    @XmlElement(name = "room")
    private List<RoomReportEntry> rooms = new ArrayList<>();

    @XmlElementWrapper(name = "tasks")
    @XmlElement(name = "task")
    private List<TaskReportEntry> tasks = new ArrayList<>();

    @XmlElementWrapper(name = "reservations")
    @XmlElement(name = "reservation")
    private List<ReservationReportEntry> reservations = new ArrayList<>();
}
