package com.bluesky.bugtraker.service;

import com.bluesky.bugtraker.shared.dto.CommentDto;
import com.bluesky.bugtraker.shared.dto.ProjectDto;
import com.bluesky.bugtraker.shared.dto.TicketDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import com.bluesky.bugtraker.view.model.request.ProjectRequestModel;
import com.bluesky.bugtraker.view.model.request.SubscriberRequestModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

public interface ProjectService {
    ProjectDto getProject(String projectId);

    ProjectDto createProject(String creatorId, ProjectDto projectDto);

    ProjectDto setProjectName(String projectId, ProjectRequestModel projectRequestBody);

    void deleteProject(String projectId);

    void addTicket(String projectId, TicketDto ticketDto);

    void removeTicket(String projectId, TicketDto ticketDto);

    void addSubscriber(String projectId, SubscriberRequestModel subscriber);

    ProjectDto removeSubscriber(String projectId, String subscriberId);

    DataTablesOutput<UserDto> getSubscribers(String projectId, DataTablesInput input);

    void createComment( String projectId, String commentCreatorId, CommentDto map);

    Page<CommentDto> getComments(String projectId, int page, int limit, String sortBy, Sort.Direction direction);
    DataTablesOutput<ProjectDto> getProjects(String creatorId, DataTablesInput input);
}
