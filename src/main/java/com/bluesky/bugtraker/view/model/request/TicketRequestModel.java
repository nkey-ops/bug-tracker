package com.bluesky.bugtraker.view.model.request;

import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@Getter
@Setter
public class TicketRequestModel {
    @NotNull
    private Status status;
    @NotNull
    private Severity severity;
    @NotNull
    private Priority priority;

    @NotEmpty
    @Size(min = 4, max = 3000,
            message = "Short description length should be not less than 4 characters and no more than 3000")
    private String shortDescription;
   
    @NotEmpty
    @Size(min = 4, max = 3000,
            message = "How to Reproduce description length should be not less than 4 characters and no more than 3000")
    private String howToReproduce;
   
    @NotEmpty
    @Size(min = 4, max = 3000,
            message = "Erroneous Program Behaviour length should be not less than 4 characters and no more than 3000")
    private String erroneousProgramBehaviour;

    @Size(max = 3000,
            message = "Solution length should not be more than 300")
    private String howToSolve;
}
