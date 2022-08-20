package com.bluesky.bugtraker.view.model.request;

import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Date;


@Getter @Setter
public class TicketRequestModel {
    private Status status;
    private Severity severity;
    private Priority priority;

    private Date reportedTime;

    private String shortDescription;
    private String howToReproduce;
    private String erroneousProgramBehaviour;


}
