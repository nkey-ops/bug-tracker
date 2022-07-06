package com.bluesky.bugtraker.service;

import com.bluesky.bugtraker.shared.dto.ProjectDto;
import com.bluesky.bugtraker.view.model.request.ProjectRequestBody;

import java.util.Set;

public interface ProjectService{
    ProjectDto getProject(String userId, String projectName);

    ProjectDto createProject(String userId, ProjectDto projectDto);

    ProjectDto setProjectName(String userId, String projectName, ProjectRequestBody projectRequestBody);

    void deleteProject(String userId, String projectName);

    Set<ProjectDto> getProjects(String userId, int pages, int limit);
}
