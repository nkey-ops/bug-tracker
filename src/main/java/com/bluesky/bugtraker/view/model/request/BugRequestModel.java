package com.bluesky.bugtraker.view.model.request;

import com.bluesky.bugtraker.shared.bugstatus.Priority;
import com.bluesky.bugtraker.shared.bugstatus.Severity;
import com.bluesky.bugtraker.shared.bugstatus.Status;
import com.bluesky.bugtraker.shared.dto.UserDto;
import lombok.Getter;
import lombok.Setter;


@Getter @Setter
public class BugRequestModel {
    private Status status;
    private Severity severity;
    private Priority priority;
    //report by generate by server
//    private UserDto reportedBy;
    private String howToReproduce;
    private String erroneousProgramBehaviour;
}
