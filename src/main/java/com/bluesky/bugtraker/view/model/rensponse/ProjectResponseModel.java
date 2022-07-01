package com.bluesky.bugtraker.view.model.rensponse;

import com.bluesky.bugtraker.io.entity.UserEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter @Setter
public class ProjectResponseModel {
    private String name;
    private UserResponseModel createdBy;
    private Set<BugResponseModel> bugs = new LinkedHashSet<>();
    private Set<UserResponseModel> subscribers = new LinkedHashSet<>();
}
