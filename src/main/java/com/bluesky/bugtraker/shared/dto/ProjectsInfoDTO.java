package com.bluesky.bugtraker.shared.dto;

import com.bluesky.bugtraker.shared.ticketstatus.Status;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectsInfoDTO {
  private long ticketsReported;
  private long ticketsReportedADayAgo;

  private long criticalTickets;
  private long criticalTicketsADayAgo;

  private long completedTickets;
  private long completedTicketsADayAgo;

  private long ticketsInProgress;
  private long ticketInProgressADayAgo;

  private Map<Status, List<Long>> ticketsStatusPerWeek;
  private Map<Status, Long> ticketsStatusOverall;
}
