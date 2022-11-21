package com.bluesky.bugtraker.service;

import com.bluesky.bugtraker.shared.dto.CommentDTO;
import com.bluesky.bugtraker.shared.dto.ProjectDTO;
import com.bluesky.bugtraker.shared.dto.UserDTO;
import com.bluesky.bugtraker.view.model.request.SubscriberRequestModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

public interface ProjectService {
    void createProject(String creatorId, ProjectDTO projectDto);

    ProjectDTO updateProject(String projectId, ProjectDTO projectDto);

    ProjectDTO getProject(String projectId);

    void deleteProject(String projectId);

    void addSubscriber(String projectId, SubscriberRequestModel subscriber);

    DataTablesOutput<UserDTO> getSubscribers(String projectId, DataTablesInput input);

    void removeSubscriber(String projectId, String subscriberId);

    void createComment( String projectId, String commentCreatorId, CommentDTO map);

    Page<CommentDTO> getComments(String projectId, int page, int limit, String sortBy, Sort.Direction direction);
    DataTablesOutput<ProjectDTO> getProjects(String creatorId, DataTablesInput input);

    DataTablesOutput<ProjectDTO> getSubscribedToProjects(String userId, DataTablesInput input);
}
