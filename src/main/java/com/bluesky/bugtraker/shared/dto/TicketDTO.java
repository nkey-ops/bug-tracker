package com.bluesky.bugtraker.shared.dto;

import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketDTO implements Serializable {
  @Serial
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private static final long serialVersionUID = 5211376248151308747L;

  private String publicId;
  private Status status;
  private Severity severity;
  private Priority priority;
  private Date createdTime;
  private Date lastUpdateTime;
  private String shortDescription;
  private String howToReproduce;
  private String erroneousProgramBehaviour;
  private String howToSolve;
  private ProjectDTO project;
  private UserDTO reporter;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TicketDTO ticketDTO = (TicketDTO) o;
    return Objects.equals(publicId, ticketDTO.publicId)
        && status == ticketDTO.status
        && severity == ticketDTO.severity
        && priority == ticketDTO.priority
        && Objects.equals(createdTime, ticketDTO.createdTime)
        && Objects.equals(lastUpdateTime, ticketDTO.lastUpdateTime)
        && Objects.equals(project, ticketDTO.project)
        && Objects.equals(shortDescription, ticketDTO.shortDescription)
        && Objects.equals(howToReproduce, ticketDTO.howToReproduce)
        && Objects.equals(erroneousProgramBehaviour, ticketDTO.erroneousProgramBehaviour)
        && Objects.equals(howToSolve, ticketDTO.howToSolve)
        && Objects.equals(reporter, ticketDTO.reporter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        publicId,
        status,
        severity,
        priority,
        createdTime,
        lastUpdateTime,
        project,
        shortDescription,
        howToReproduce,
        erroneousProgramBehaviour,
        howToSolve,
        reporter);
  }
}
