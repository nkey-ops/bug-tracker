package com.bluesky.bugtraker.view.model.request;

import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketRequestModel {
  @NotNull private Status status;
  @NotNull private Severity severity;
  @NotNull private Priority priority;

  @NotEmpty
  @Size(
      min = 4,
      max = 60,
      message = "Short description length should be not less than 4 characters and no more than 60")
  private String shortDescription;

  @NotEmpty
  @Size(
      min = 4,
      max = 5000,
      message =
          "How to Reproduce description length should be not less than 4 characters and no more"
              + " than 5000")
  private String howToReproduce;

  @NotEmpty
  @Size(
      min = 4,
      max = 5000,
      message =
          "Erroneous Program Behaviour length should be not less than 4 characters and no more than"
              + " 5000")
  private String erroneousProgramBehaviour;

  @Size(max = 5000, message = "Solution length should not be more than 5000")
  private String howToSolve;
}
