package com.bluesky.bugtraker.view.model.request;

import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;


@Getter
@Setter
public class TicketRequestModel {
    @NotNull
    private Status status;
    @NotNull
    private Severity severity;
    @NotNull
    private Priority priority;

    @NotNull
    private String shortDescription;
    @NotNull
    private String howToReproduce;
    @NotNull
    private String howToSolve;
    @NotNull
    private String erroneousProgramBehaviour;


}
