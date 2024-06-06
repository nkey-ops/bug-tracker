package com.bluesky.bugtraker.service.utils;

import static com.bluesky.bugtraker.io.specification.Specs.*;

import com.bluesky.bugtraker.io.entity.TicketEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.repository.TicketRepository;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class UserServiceUtils {

  private final TicketRepository ticketRepo;

  public UserServiceUtils(TicketRepository ticketRepo) {
    this.ticketRepo = ticketRepo;
  }

  public Map<Severity, Long> getTicketsSeverityOverall(UserEntity userEntity) {
    Map<Severity, Long> tickets = new EnumMap<>(Severity.class);
    tickets.put(
        Severity.LOW, ticketRepo.countAllByProjectCreatorAndSeverity(userEntity, Severity.LOW));
    tickets.put(
        Severity.MINOR, ticketRepo.countAllByProjectCreatorAndSeverity(userEntity, Severity.MINOR));
    tickets.put(
        Severity.MAJOR, ticketRepo.countAllByProjectCreatorAndSeverity(userEntity, Severity.MAJOR));
    tickets.put(
        Severity.CRITICAL,
        ticketRepo.countAllByProjectCreatorAndSeverity(userEntity, Severity.CRITICAL));
    return tickets;
  }

  public Map<Severity, List<Long>> getTicketsSeverityPerWeek(UserEntity userEntity) {
    Map<Severity, List<Long>> ticketsSeverityPerWeek = new EnumMap<>(Severity.class);
    ticketsSeverityPerWeek.putIfAbsent(Severity.LOW, new ArrayList<>());
    ticketsSeverityPerWeek.putIfAbsent(Severity.MINOR, new ArrayList<>());
    ticketsSeverityPerWeek.putIfAbsent(Severity.MAJOR, new ArrayList<>());
    ticketsSeverityPerWeek.putIfAbsent(Severity.CRITICAL, new ArrayList<>());

    for (int i = 0; i < 5; i++) {
      Specification<TicketEntity> ticketByCreatorAndLastUpdate =
          ticketByProjectCreator(userEntity).and(lastUpdatedWeeksAgo(i));

      long countLow = ticketRepo.count(ticketByCreatorAndLastUpdate.and(severityIs(Severity.LOW)));
      long countMinor =
          ticketRepo.count(ticketByCreatorAndLastUpdate.and(severityIs(Severity.MINOR)));
      long countMajor =
          ticketRepo.count(ticketByCreatorAndLastUpdate.and(severityIs(Severity.MAJOR)));
      long countCritical =
          ticketRepo.count(ticketByCreatorAndLastUpdate.and(severityIs(Severity.CRITICAL)));

      ticketsSeverityPerWeek.get(Severity.LOW).add(countLow);
      ticketsSeverityPerWeek.get(Severity.MINOR).add(countMinor);
      ticketsSeverityPerWeek.get(Severity.MAJOR).add(countMajor);
      ticketsSeverityPerWeek.get(Severity.CRITICAL).add(countCritical);
    }
    return ticketsSeverityPerWeek;
  }

  public Map<Status, Long> getTicketsStatusOverall(UserEntity userEntity) {
    Map<Status, Long> tickets = new EnumMap<>(Status.class);
    tickets.put(
        Status.TO_FIX, ticketRepo.countAllByProjectCreatorAndStatus(userEntity, Status.TO_FIX));
    tickets.put(
        Status.IN_PROGRESS,
        ticketRepo.countAllByProjectCreatorAndStatus(userEntity, Status.IN_PROGRESS));
    tickets.put(
        Status.COMPLETED,
        ticketRepo.countAllByProjectCreatorAndStatus(userEntity, Status.COMPLETED));
    return tickets;
  }

  public Map<Status, List<Long>> getTicketsStatusPerWeek(UserEntity userEntity) {
    Map<Status, List<Long>> ticketsPerWeek = new EnumMap<>(Status.class);

    ticketsPerWeek.putIfAbsent(Status.TO_FIX, new ArrayList<>());
    ticketsPerWeek.putIfAbsent(Status.IN_PROGRESS, new ArrayList<>());
    ticketsPerWeek.putIfAbsent(Status.COMPLETED, new ArrayList<>());

    for (int i = 0; i < 5; i++) {
      Specification<TicketEntity> ticketByCreatorAndLastUpdate =
          ticketByProjectCreator(userEntity).and(lastUpdatedWeeksAgo(i));

      long countToFix = ticketRepo.count(ticketByCreatorAndLastUpdate.and(statusIs(Status.TO_FIX)));
      long countInProgress =
          ticketRepo.count(ticketByCreatorAndLastUpdate.and(statusIs(Status.IN_PROGRESS)));
      long countCompleted =
          ticketRepo.count(ticketByCreatorAndLastUpdate.and(statusIs(Status.COMPLETED)));

      ticketsPerWeek.get(Status.TO_FIX).add(countToFix);
      ticketsPerWeek.get(Status.IN_PROGRESS).add(countInProgress);
      ticketsPerWeek.get(Status.COMPLETED).add(countCompleted);
    }
    return ticketsPerWeek;
  }
}
