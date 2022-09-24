package com.bluesky.bugtraker.shared.dto;

import lombok.Getter;
import lombok.Setter;

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
    
}
