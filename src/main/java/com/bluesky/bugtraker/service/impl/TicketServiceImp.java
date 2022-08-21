package com.bluesky.bugtraker.service.impl;

import com.bluesky.bugtraker.exceptions.serviceexception.TicketServiceException;
import com.bluesky.bugtraker.io.entity.*;
import com.bluesky.bugtraker.io.repository.CommentRepository;
import com.bluesky.bugtraker.io.repository.TicketHistoryRepository;
import com.bluesky.bugtraker.io.repository.TicketRepository;
import com.bluesky.bugtraker.service.ProjectService;
import com.bluesky.bugtraker.service.TicketService;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.shared.Utils;
import com.bluesky.bugtraker.shared.dto.*;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Set;

import static com.bluesky.bugtraker.exceptions.ErrorMessages.*;

@Service
public class TicketServiceImp implements TicketService {
    private final TicketRepository ticketRepo;
    private final TicketHistoryRepository ticketRecordRepo;
    private final CommentRepository commentRepo;
    private final UserService userService;
    private final ProjectService projectService;
    private final Utils utils;

    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public TicketServiceImp(TicketRepository ticketRepo,
                            TicketHistoryRepository ticketRecordRepo,
                            CommentRepository commentRepo,
                            UserService userService,
                            Utils utils,
                            ProjectService projectService) {
        this.ticketRepo = ticketRepo;
        this.ticketRecordRepo = ticketRecordRepo;
        this.commentRepo = commentRepo;
        this.userService = userService;
        this.utils = utils;
        this.projectService = projectService;

        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    }

    //  TODO make bugId unique in bounds of the project  and change this method
    private TicketEntity getTicketEntity(String bugId) {

        return ticketRepo.findByPublicId(bugId)
                .orElseThrow(() -> new TicketServiceException(NO_RECORD_FOUND, bugId));
    }

    @Override
    public TicketDto getTicket(String bugId) {

        TicketEntity ticketEntity = getTicketEntity(bugId);

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
    public void createTicket(String userId, String projectName, TicketDto ticketDto, String reporterId) {
        ProjectEntity projectEntity =
                modelMapper.map(
                        projectService.getProject(userId, projectName),
                        ProjectEntity.class);

        if (ticketRepo.existsByProjectAndPublicId(projectEntity, ticketDto.getPublicId()))
            throw new TicketServiceException(RECORD_ALREADY_EXISTS, ticketDto.getPublicId());

        TicketEntity ticketEntity = modelMapper.map(ticketDto, TicketEntity.class);

        ticketEntity.setPublicId(utils.generateBugId(10));
        ticketEntity.setReportedTime(Date.from(Instant.now()));
        ticketEntity.setLastUpdateTime(Date.from(Instant.now()));
        ticketEntity.setHowToSolve("Solution is not found");

        ticketEntity.setReporter(
                modelMapper.map(
                        userService.getUserById(reporterId), UserEntity.class));
        ticketEntity.setProject(
                modelMapper.map(
                        projectService.getProject(userId, projectName), ProjectEntity.class));

        TicketEntity savedTicketEntity = ticketRepo.save(ticketEntity);

        createTicketRecord(savedTicketEntity);
    }


    @Override
    public void updateTicket(String userId, String projectName, String bugId, TicketDto ticketDtoUpdates) {
        TicketEntity ticketEntity = getTicketEntity(bugId);

        modelMapper.map(ticketDtoUpdates, ticketEntity);
        ticketEntity.setLastUpdateTime(Date.from(Instant.now()));

        TicketEntity savedTicketEntity = ticketRepo.save(ticketEntity);

        createTicketRecord(savedTicketEntity);
    }

    @Override
    public void deleteBug(String userId, String projectName, String ticketId) {
        TicketDto ticketDto = getTicket(ticketId);

        projectService.removeBug(userId, projectName, ticketDto);
    }

    private void createTicketRecord(TicketEntity mainTicketEntity) {
        TicketRecordEntity ticketRecordEntity =
                modelMapper.map(mainTicketEntity, TicketRecordEntity.class);

        ticketRecordEntity.setId(null);
        ticketRecordEntity.setPublicId(utils.generateTicketRecordId(15));
        ticketRecordEntity.setMainTicket(mainTicketEntity);


        ticketRecordRepo.save(ticketRecordEntity);
    }

    @Override
    public Page<TicketRecordDto> getTicketRecords(String ticketId, int page, int limit) {
        if (page < 1 || limit < 1) throw new IllegalArgumentException();

        Page<TicketRecordEntity> pagedTicketHistoryEntities =
                ticketRecordRepo.findAllByMainTicket(
                        getTicketEntity(ticketId),
                        PageRequest.of(page - 1, limit)
                );
        return modelMapper.map(pagedTicketHistoryEntities, new TypeToken<Page<ProjectDto>>() {
        }.getType());

    }

    @Override
    public TicketRecordDto getTicketRecord(String recordId) {
        TicketRecordEntity ticketRecordEntity = ticketRecordRepo.findByPublicId(recordId);

        return modelMapper.map(ticketRecordEntity, TicketRecordDto.class);
    }

    @Override
    public void addAssignedDev(String ticketId, String assignedDevId) {
        TicketEntity ticketEntity = getTicketEntity(ticketId);

        UserDto assignedDevDto = userService.getUserById(assignedDevId);
        boolean isAdded = ticketEntity.addAssignedDev(
                modelMapper.map(assignedDevDto, UserEntity.class));

        if (!isAdded)
            throw new TicketServiceException(RECORD_ALREADY_ADDED, assignedDevId);

        ticketRepo.save(ticketEntity);
    }

    @Override
    public void removeAssignedDev(String ticketId, String assignedDevId) {
        TicketEntity ticketEntity = getTicketEntity(ticketId);

        UserDto assignedDevDto = userService.getUserById(assignedDevId);
        UserEntity assignedDevEntity = modelMapper.map(assignedDevDto, UserEntity.class);

        boolean isRemoved = ticketEntity.removeAssignedDev(assignedDevEntity);

        if (!isRemoved)
            throw new TicketServiceException(NO_RECORD_FOUND, assignedDevId);

        ticketRepo.save(ticketEntity);
    }

    @Override
    public Page<UserDto> getAssignedDevs(String ticketId,
                                         int page, int limit) {
        if (page < 1 || limit < 1) throw new IllegalArgumentException();

        Set<UserEntity> assignedDevs = getTicketEntity(ticketId).getAssignedDevs();

        Set<UserDto> assignedDevDtos =
                modelMapper.map(assignedDevs, new TypeToken<Set<UserDto>>() {}.getType());

        return new PageImpl<>(
                        assignedDevDtos.stream().toList(),
                        Pageable.ofSize(page), limit);
    }

    @Override
    public void createComment(String userId, String projectName, String ticketId, CommentDto comment) {
        CommentEntity commentEntity = modelMapper.map(comment, CommentEntity.class);

        commentEntity.setPublicId(utils.generateCommentId(10));
        commentEntity.setUploadTime(Date.from(Instant.now()));

        commentEntity.setTicket(getTicketEntity(ticketId));
        commentEntity.setUser(modelMapper.map(userService.getUserById(userId), UserEntity.class));

        commentRepo.save(commentEntity);

    }


    @Override
    public Page<CommentDto> getComments(String userId, String projectName,
                                        String ticketId,
                                        int page, int limit,
                                        String sortBy, String direction) {
        if (page < 1 || limit < 1) throw new IllegalArgumentException();

        TicketEntity ticketEntity = getTicketEntity(ticketId);

        PageRequest pageRequest =
                PageRequest.of(page - 1, limit, Sort.Direction.fromString(direction), sortBy);


        Page<CommentEntity> commentEntities =
                commentRepo.findAllByTicket(ticketEntity, pageRequest);

        return modelMapper.map(commentEntities, new TypeToken<Page<CommentDto>>() {
        }.getType());
    }
}
