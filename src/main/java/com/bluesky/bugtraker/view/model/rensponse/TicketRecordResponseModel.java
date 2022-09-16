package com.bluesky.bugtraker.view.model.rensponse;

import com.bluesky.bugtraker.shared.dto.TicketDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import com.bluesky.bugtraker.view.model.rensponse.ProjectResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
public class TicketRecordResponseModel {
    private String publicId;
    
    private Status status;
    private Severity severity;
    private Priority priority;

    private String shortDescription;
    private String howToReproduce;
    private String erroneousProgramBehaviour;
    private String howToSolve;

    private Date reportedTime;
    private Date lastUpdateTime;
    
    private TicketRecordResponseModel mainTicket;
    private UserResponseModel creator;
}

