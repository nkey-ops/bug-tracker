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
public class TicketRecordDTO implements Serializable {
  @Serial
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private static final long serialVersionUID = 8191538905244886046L;

  private Long id;
  private String publicId;
  private Status status;
  private Severity severity;
  private Priority priority;

  private String shortDescription;
  private String howToReproduce;
  private String erroneousProgramBehaviour;
  private String howToSolve;

  private Date createdTime;

  private TicketDTO mainTicket;
  private UserDTO creator;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TicketRecordDTO that = (TicketRecordDTO) o;
    return Objects.equals(id, that.id)
        && Objects.equals(publicId, that.publicId)
        && status == that.status
        && severity == that.severity
        && priority == that.priority
        && Objects.equals(createdTime, that.createdTime)
        && Objects.equals(shortDescription, that.shortDescription)
        && Objects.equals(howToReproduce, that.howToReproduce)
        && Objects.equals(erroneousProgramBehaviour, that.erroneousProgramBehaviour)
        && Objects.equals(howToSolve, that.howToSolve);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
        publicId,
        status,
        severity,
        priority,
        createdTime,
        shortDescription,
        howToReproduce,
        erroneousProgramBehaviour,
        howToSolve);
  }
}
