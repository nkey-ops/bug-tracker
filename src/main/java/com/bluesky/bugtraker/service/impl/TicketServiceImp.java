package com.bluesky.bugtraker.service.impl;

import com.bluesky.bugtraker.exceptions.serviceexception.BugServiceException;
import com.bluesky.bugtraker.io.entity.CommentEntity;
import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.TicketEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.repository.CommentRepository;
import com.bluesky.bugtraker.io.repository.TicketRepository;
import com.bluesky.bugtraker.service.ProjectService;
import com.bluesky.bugtraker.service.TicketService;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.shared.Utils;
import com.bluesky.bugtraker.shared.dto.CommentDto;
import com.bluesky.bugtraker.shared.dto.TicketDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.bluesky.bugtraker.exceptions.ErrorMessages.*;

@Service
public class TicketServiceImp implements TicketService {
    private final TicketRepository ticketRepo;
    private final CommentRepository commentRepo;
    private final UserService userService;
    private final ProjectService projectService;
    private final Utils utils;

    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public TicketServiceImp(TicketRepository ticketRepo, CommentRepository commentRepo, UserService userService,
                            Utils utils, ProjectService projectService) {
        this.ticketRepo = ticketRepo;
        this.commentRepo = commentRepo;
        this.userService = userService;
        this.utils = utils;
        this.projectService = projectService;

        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    }

    //  TODO make bugId unique in bounds of the project  and change this method
    private TicketEntity getTicketEntity(String userId,
                                         String projectName,
                                         String bugId) {

        return ticketRepo.findByPublicId(bugId)
                .orElseThrow(() -> new BugServiceException(NO_RECORD_FOUND, bugId));
    }

    @Override
    public TicketDto getTicket(String userId,
                               String projectName,
                               String bugId) {

        TicketEntity ticketEntity = getTicketEntity(userId, projectName, bugId);

        return modelMapper.map(ticketEntity, TicketDto.class);
    }

    @Override
    public Set<TicketDto> getTickets(String userId, String projectName, int page, int limit) {
        if (page-- < 0 || limit < 1) throw new IllegalArgumentException();

        ProjectEntity projectEntity = modelMapper.map(projectService.getProject(userId, projectName),
                ProjectEntity.class);

//        Page<BugEntity> pagedBugsEntities = bugRepo.findAllByProject(projectEntity, PageRequest.of(page, limit));
        Set<TicketEntity> pagedBugsEntities = ticketRepo.findAllByProject(projectEntity);

        return modelMapper.map(pagedBugsEntities, new TypeToken<Set<TicketDto>>() {
        }.getType());

    }

    @Override
    public TicketDto createTicket(String userId, String projectName, TicketDto ticketDto, String reporterId) {
        ProjectEntity projectEntity =
                modelMapper.map(
                        projectService.getProject(userId, projectName),
                        ProjectEntity.class);

        if (ticketRepo.existsByProjectAndPublicId(projectEntity, ticketDto.getPublicId()))
            throw new BugServiceException(RECORD_ALREADY_EXISTS, ticketDto.getPublicId());

        TicketEntity ticketEntity = modelMapper.map(ticketDto, TicketEntity.class);

        ticketEntity.setPublicId(utils.generateBugId(10));
        ticketEntity.setReportedTime(Date.from(Instant.now()));

        ticketEntity.setReporter(
                modelMapper.map(
                        userService.getUserById(reporterId), UserEntity.class));
        ticketEntity.setProject(
                modelMapper.map(
                        projectService.getProject(userId, projectName), ProjectEntity.class));

        TicketEntity savedTicketEntity = ticketRepo.save(ticketEntity);

        return  modelMapper.map(savedTicketEntity, TicketDto.class);

        //        projectService.addBug(userId, projectName, ticketDto);

//        return getTicket(userId, projectName, ticketDto.getPublicId());
    }


    @Override
    public void updateBug(String userId, String projectName, String bugId, TicketDto ticketDtoUpdates) {
        TicketEntity ticketEntity = getTicketEntity(userId, projectName, bugId);

        modelMapper.map(ticketEntity, ticketDtoUpdates);

        ticketRepo.save(ticketEntity);
    }

    @Override
    public void deleteBug(String userId, String projectName, String bugId) {
        TicketDto ticketDto = getTicket(userId, projectName, bugId);

        projectService.removeBug(userId, projectName, ticketDto);
    }

    @Override
    public void addBugFixer(String userId, String projectName, String bugId, String fixerId) {
        TicketEntity ticketEntity = getTicketEntity(userId, projectName, bugId);

        com.bluesky.bugtraker.shared.dto.UserDto fixerDto = userService.getUserById(fixerId);
        boolean isAdded = ticketEntity.addBugFixer(
                modelMapper.map(fixerDto, UserEntity.class));

        if (!isAdded)
            throw new BugServiceException(RECORD_ALREADY_ADDED, fixerId);

        ticketRepo.save(ticketEntity);
    }

    @Override
    public void removeBugFixer(String userId, String projectName, String bugId, String fixerId) {
        TicketEntity ticketEntity = getTicketEntity(userId, projectName, bugId);

        com.bluesky.bugtraker.shared.dto.UserDto fixerDto = userService.getUserById(fixerId);
        boolean isRemoved = ticketEntity.removeBugFixer(
                modelMapper.map(fixerDto, UserEntity.class));

        if (!isRemoved)
            throw new BugServiceException(NO_RECORD_FOUND, fixerId);

        ticketRepo.save(ticketEntity);
    }

    @Override
    public Set<com.bluesky.bugtraker.shared.dto.UserDto> getBugFixers(String userId, String projectName, String bugId,
                                                                      int page, int limit) {


        Set<com.bluesky.bugtraker.shared.dto.UserDto> bugFixers = getTicket(userId, projectName, bugId).getBugFixers();

        PageImpl<UserDto> pagedUsers =
                new PageImpl<>(bugFixers.stream().toList(),
                        Pageable.ofSize(page), limit);

        return new LinkedHashSet<>(pagedUsers.getContent());
    }

    @Override
    public void createComment(String userId, String projectName, String ticketId, CommentDto comment) {
        CommentEntity commentEntity = modelMapper.map(comment, CommentEntity.class);

        commentEntity.setPublicId(utils.generateCommentId(10));
        commentEntity.setUploadTime(Date.from(Instant.now()));

        commentEntity.setTicket(getTicketEntity(userId, projectName, ticketId));
        commentEntity.setUser(modelMapper.map(userService.getUserById(userId), UserEntity.class));

        commentRepo.save(commentEntity);

    }

    @Override
    public Page<CommentDto> getComments(String userId, String projectName,
                                        String tickerId,
                                        int page, int limit,
                                        String sortBy, String direction) {
        if (page < 1 || limit < 1) throw new IllegalArgumentException();

        TicketEntity ticketEntity = getTicketEntity(userId, projectName, tickerId);

        PageRequest pageRequest =
                PageRequest.of(page - 1, limit, Sort.Direction.fromString(direction), sortBy);


        Page<CommentEntity> commentEntities =
                commentRepo.findAllByTicket(ticketEntity, pageRequest);

        return modelMapper.map(commentEntities, new TypeToken<Page<CommentDto>>() {}.getType());
    }
}
