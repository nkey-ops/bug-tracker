package com.bluesky.bugtraker.view.model.rensponse;

import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

import java.util.Date;

@Getter @Setter
public class TicketRecordResponseModel extends RepresentationModel<TicketResponseModel> {
    private String publicId;
    
    private Status status;
    private Severity severity;
    private Priority priority;

    private String shortDescription;
    private String howToReproduce;
    private String erroneousProgramBehaviour;
    private String howToSolve;

    private Date createdTime;
    
    private TicketResponseModel mainTicket;
    private UserResponseModel creator;

}

