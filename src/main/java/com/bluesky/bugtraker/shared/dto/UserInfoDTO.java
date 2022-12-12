package com.bluesky.bugtraker.shared.dto;

import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter @Setter
public class UserInfoDTO {
    private long ticketsReported;
    private long ticketsReportedADayAgo;
    private long ticketsReportedAMonthAgo;
    
    private long ticketsCompleted;
    private long ticketsCompletedADayAgo;
    private long ticketsCompletedAMonthAgo;
    
    private long ticketsSubscribedTo;
    private long ticketsSubscribedToADayAgo;
    private long ticketsSubscribedToAMonthAgo;

    private Map<Severity, List<Long>> ticketsSeverityPerWeek;
    private Map<Severity, Long> ticketsSeverityOverall;
    
}
