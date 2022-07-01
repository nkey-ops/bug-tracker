package com.bluesky.bugtraker.service;

import com.bluesky.bugtraker.shared.dto.ProjectDto;

public interface ProjectService{
    ProjectDto getProject(String userId, String projectName);

    ProjectDto createProject(String userId, String projectName);

    ProjectDto setProjectName(String userId, String projectName, String newName);

    void deleteProject(String userId, String projectName);
}
