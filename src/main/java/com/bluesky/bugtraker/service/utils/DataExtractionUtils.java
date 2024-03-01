package com.bluesky.bugtraker.service.utils;

import static com.bluesky.bugtraker.exceptions.ErrorType.INTERNAL_SERVER_ERROR;
import static com.bluesky.bugtraker.exceptions.ErrorType.NO_RECORD_FOUND;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bluesky.bugtraker.exceptions.serviceexception.ProjectServiceException;
import com.bluesky.bugtraker.exceptions.serviceexception.RoleServiceException;
import com.bluesky.bugtraker.exceptions.serviceexception.TicketServiceException;
import com.bluesky.bugtraker.exceptions.serviceexception.UserServiceException;
import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.RoleEntity;
import com.bluesky.bugtraker.io.entity.TicketEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.repository.ProjectRepository;
import com.bluesky.bugtraker.io.repository.RoleRepository;
import com.bluesky.bugtraker.io.repository.TicketRepository;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.shared.authorizationenum.Role;

import jakarta.validation.constraints.NotNull;

@Component
public class DataExtractionUtils {
    private final UserRepository userRepo;
    private final ProjectRepository projectRepo;
    private final TicketRepository ticketRepo;
    private final RoleRepository roleRepo;

    @Autowired
    public DataExtractionUtils(UserRepository userRepo,
                               ProjectRepository projectRepo,
                               TicketRepository ticketRepo, RoleRepository roleRepo) {
        this.userRepo = userRepo;
        this.projectRepo = projectRepo;
        this.ticketRepo = ticketRepo;
        this.roleRepo = roleRepo;
    }

    @NotNull
    public UserEntity getUserEntity(@NotNull String id) {
        return userRepo.findByPublicId(id)
                .orElseThrow(() -> new UserServiceException(NO_RECORD_FOUND, id));
    }

    @NotNull
    public ProjectEntity getProjectEntity(@NotNull String projectId) {
        return projectRepo.findByPublicId(projectId)
                .orElseThrow(() -> new ProjectServiceException(NO_RECORD_FOUND, projectId));
    }
    

    @NotNull
    public RoleEntity getRoleEntityToBeSet(@NotNull Role roleToBeSet, @NotNull Role currentRole) {
        if(currentRole == Role.ROLE_SUPER_ADMIN
                && userRepo.countByRoleEntityRole(Role.ROLE_SUPER_ADMIN) <= 1)
       
            throw new UserServiceException(INTERNAL_SERVER_ERROR,
                    "At least one user with role " + Role.ROLE_SUPER_ADMIN + " should exists");


        return roleRepo.findByRole(roleToBeSet)
                .orElseThrow(() ->
                        new RoleServiceException(NO_RECORD_FOUND, roleToBeSet.toString()));
    }

    @NotNull
    public TicketEntity getTicketEntity(@NotNull String ticketId) {
        return ticketRepo.findByPublicId(ticketId)
                .orElseThrow(() -> new TicketServiceException(NO_RECORD_FOUND, ticketId));
    }
}
