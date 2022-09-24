package com.bluesky.bugtraker.shared.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketsInfoDTO {
    private long ticketsReported;
    private long ticketsReportedADayAgo;

    private long criticalTickets;
    private long criticalTicketsADayAgo;

    private long completedTickets;
    private long completedTicketsADayAgo;

    private long ticketsInProgress;
    private long ticketInProgressADayAgo;

}
