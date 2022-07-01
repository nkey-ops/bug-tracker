package com.bluesky.bugtraker.shared.dto;

import com.bluesky.bugtraker.io.entity.UserEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter @Setter
public class ProjectDto {
    private Long id;
    private String name;
    private UserDto createdBy;
    private Set<BugDto> bugs;
    private Set<UserDto> subscribers;
}
