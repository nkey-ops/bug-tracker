package com.bluesky.bugtraker.view.model.request;

import com.bluesky.bugtraker.shared.bugstatus.Priority;
import com.bluesky.bugtraker.shared.bugstatus.Severity;
import com.bluesky.bugtraker.shared.bugstatus.Status;
import com.bluesky.bugtraker.shared.dto.UserDto;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter @Setter
public class BugRequestModel {
    private Status status;
    private Severity severity;
    private Priority priority;

    private String shortDescription;
    private String howToReproduce;
    private String erroneousProgramBehaviour;

    private List<UserRequestModel> bugFixers;

}
