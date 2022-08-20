package com.bluesky.bugtraker.service;

import com.bluesky.bugtraker.shared.dto.TicketDto;
import com.bluesky.bugtraker.shared.dto.ProjectDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import com.bluesky.bugtraker.view.model.request.ProjectRequestModel;
import com.bluesky.bugtraker.view.model.request.SubscriberRequestModel;

import java.util.Set;

public interface ProjectService{
    ProjectDto getProject(String userId, String projectName);

    Set<ProjectDto> getProjects(String userId, int pages, int limit);

    ProjectDto createProject(String userId, ProjectDto projectDto);

    ProjectDto setProjectName(String userId, String projectName, ProjectRequestModel projectRequestBody);

    void deleteProject(String userId, String projectName);

    void addBug(String userId, String projectName, TicketDto ticketDto);

    void removeBug(String userId, String projectName, TicketDto ticketDto);

    void addSubscriber(String userId, String projectName, SubscriberRequestModel subscriber);

    ProjectDto removeSubscriber(String userId, String projectName, String subscriberId);

    Set<UserDto> getSubscribers(String userId, String projectName);


}
