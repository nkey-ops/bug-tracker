package com.bluesky.bugtraker.view.model.rensponse;

import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import java.util.Date;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@Setter
public class TicketResponseModel extends RepresentationModel<TicketResponseModel> {
  private String publicId;
  private Status status;
  private Severity severity;
  private Priority priority;

  private String shortDescription;
  private String howToReproduce;
  private String erroneousProgramBehaviour;
  private String howToSolve;

  private Date createdTime;
  private Date lastUpdateTime;

  private UserResponseModel reporter;
  private ProjectResponseModel project;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TicketResponseModel that = (TicketResponseModel) o;
    return Objects.equals(publicId, that.publicId)
        && status == that.status
        && severity == that.severity
        && priority == that.priority
        && Objects.equals(shortDescription, that.shortDescription)
        && Objects.equals(howToReproduce, that.howToReproduce)
        && Objects.equals(erroneousProgramBehaviour, that.erroneousProgramBehaviour)
        && Objects.equals(howToSolve, that.howToSolve)
        && Objects.equals(createdTime, that.createdTime)
        && Objects.equals(lastUpdateTime, that.lastUpdateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(),
        publicId,
        status,
        severity,
        priority,
        shortDescription,
        howToReproduce,
        erroneousProgramBehaviour,
        howToSolve,
        createdTime,
        lastUpdateTime);
  }
}
