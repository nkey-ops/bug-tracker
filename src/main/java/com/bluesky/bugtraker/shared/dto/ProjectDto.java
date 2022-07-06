package com.bluesky.bugtraker.shared.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter @Setter
public class ProjectDto {
    private Long id;
    private String publicId;
    private String name;
    private UserDto creator;
    private Set<BugDto> bugs;
    private Set<UserDto> subscribers;
}
