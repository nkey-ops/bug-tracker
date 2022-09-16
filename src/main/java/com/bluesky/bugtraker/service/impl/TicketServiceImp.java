package com.bluesky.bugtraker.service.impl;

import com.bluesky.bugtraker.exceptions.serviceexception.ProjectServiceException;
import com.bluesky.bugtraker.exceptions.serviceexception.TicketServiceException;
import com.bluesky.bugtraker.io.entity.*;
import com.bluesky.bugtraker.io.repository.CommentRepository;
import com.bluesky.bugtraker.io.repository.TicketRecordsRepository;
import com.bluesky.bugtraker.io.repository.TicketRepository;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.service.ProjectService;
import com.bluesky.bugtraker.service.TicketService;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.service.Utils;
import com.bluesky.bugtraker.shared.dto.*;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static com.bluesky.bugtraker.exceptions.ErrorMessages.NO_RECORD_FOUND;
import static com.bluesky.bugtraker.exceptions.ErrorMessages.RECORD_ALREADY_ADDED;
import static com.bluesky.bugtraker.service.specifications.Specs.*;

@Service
public class TicketServiceImp implements TicketService {
    private final TicketRepository ticketRepo;
    private final TicketRecordsRepository ticketRecordRepo;
    private final CommentRepository commentRepo;
    private final UserRepository userRepo;

    private final UserService userService;
    private final ProjectService projectService;
    private final Utils utils;

    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public TicketServiceImp(TicketRepository ticketRepo,
                            TicketRecordsRepository ticketRecordRepo,
                            CommentRepository commentRepo,
                            UserRepository userRepo, UserService userService,
                            Utils utils,
                            ProjectService projectService) {
        this.ticketRepo = ticketRepo;
        this.ticketRecordRepo = ticketRecordRepo;
        this.commentRepo = commentRepo;
        this.userRepo = userRepo;
        this.userService = userService;
        this.utils = utils;
        this.projectService = projectService;

        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    }

    //  TODO make ticketId unique in bounds of the project  and change this method
    private TicketEntity getTicketEntity(String ticketId) {

        return ticketRepo.findByPublicId(ticketId)
                .orElseThrow(() -> new TicketServiceException(NO_RECORD_FOUND, ticketId));
    }

    @Override
    public TicketDto getTicket(String bugId) {

        TicketEntity ticketEntity = getTicketEntity(bugId);

        return modelMapper.map(ticketEntity, TicketDto.class);
    }

    @Override
    public DataTablesOutput<TicketDto> getTickets(String projectId, DataTablesInput input) {
        Long id = projectService.getProject(projectId).getId();

        DataTablesOutput<TicketEntity> all =
                ticketRepo.findAll(input, findAllByProjectId(id));

        DataTablesOutput<TicketDto> result = new DataTablesOutput<>();

        modelMapper.map(all, result);
        result.setData(modelMapper.map(all.getData(), new TypeToken<List<TicketDto>>() {
        }.getType()));

        return result;
    }

    @Override
    public void createTicket(String projectId, TicketDto ticketDto, String reporterId) {
        ProjectEntity projectEntity =
                modelMapper.map(
                        projectService.getProject(projectId),
                        ProjectEntity.class);

        TicketEntity ticketEntity = modelMapper.map(ticketDto, TicketEntity.class);

        ticketEntity.setPublicId(utils.generateTicketId(10));
        ticketEntity.setReportedTime(Date.from(Instant.now()));
        ticketEntity.setLastUpdateTime(Date.from(Instant.now()));
        ticketEntity.setHowToSolve("Solution is not found");

        ticketEntity.setReporter(
                modelMapper.map(
                        userService.getUserById(reporterId), UserEntity.class));

        boolean isAdded = projectEntity.addTicket(ticketEntity);
        if (!isAdded)
            throw new TicketServiceException(RECORD_ALREADY_ADDED, projectEntity.getPublicId());

        TicketEntity savedTicketEntity = ticketRepo.save(ticketEntity);

        createTicketRecord(savedTicketEntity, reporterId);
    }


    @Override
    public void updateTicket(String ticketId, TicketDto ticketDtoUpdates, String updatedByUser) {
        TicketEntity ticketEntity = getTicketEntity(ticketId);

        modelMapper.map(ticketDtoUpdates, ticketEntity);
        ticketEntity.setLastUpdateTime(Date.from(Instant.now()));

        TicketEntity savedTicketEntity = ticketRepo.save(ticketEntity);

        createTicketRecord(savedTicketEntity, updatedByUser);
    }

    @Override
    public void deleteBug(String userId, String projectId, String ticketId) {
        TicketDto ticketDto = getTicket(ticketId);

        projectService.removeTicket(projectId, ticketDto);
    }

    private void createTicketRecord(TicketEntity mainTicketEntity, String updatedByUser) {
        TicketRecordEntity ticketRecordEntity =
                modelMapper.map(mainTicketEntity, TicketRecordEntity.class);

        ticketRecordEntity.setId(null);
        ticketRecordEntity.setPublicId(utils.generateTicketRecordId(15));
        ticketRecordEntity.setMainTicket(mainTicketEntity);
        ticketRecordEntity.setCreator(
                modelMapper.map(
                        userService.getUserById(updatedByUser), UserEntity.class));


        boolean isAdded = mainTicketEntity.addTicketRecord(ticketRecordEntity);

        if (!isAdded)
            throw new TicketServiceException(RECORD_ALREADY_ADDED, mainTicketEntity.getPublicId());

        ticketRecordRepo.save(ticketRecordEntity);
    }

    @Override
    public DataTablesOutput<TicketRecordDto> getTicketRecords(String ticketId, DataTablesInput input) {
        Long id = this.getTicketEntity(ticketId).getId();

        DataTablesOutput<TicketRecordEntity> all =
                ticketRecordRepo.findAll(input, findAllTicketRecordsByTicketId(id));

        DataTablesOutput<TicketRecordDto> result = new DataTablesOutput<>();

        modelMapper.map(all, result);
        result.setData(modelMapper.map(all.getData(), new TypeToken<List<TicketRecordDto>>() {
        }.getType()));

        return result;
    }


    @Override
    public TicketRecordDto getTicketRecord(String recordId) {
        TicketRecordEntity ticketRecordEntity = ticketRecordRepo.findByPublicId(recordId);

        return modelMapper.map(ticketRecordEntity, TicketRecordDto.class);
    }

    @Override
    public void addAssignedDev(String ticketId, String userId) {

        TicketEntity ticketEntity = getTicketEntity(ticketId);
        UserDto subscriberDto = userService.getUserById(userId);

        boolean isAdded = ticketEntity.addAssignedDev(
                modelMapper.map(subscriberDto, UserEntity.class));

        if (!isAdded)
            throw new ProjectServiceException(RECORD_ALREADY_ADDED, ticketId);
        else
            ticketRepo.save(ticketEntity);
    }

    @Override
    public DataTablesOutput<UserDto> getAssignedDevs(String ticketId, DataTablesInput input) {
        Long id = getTicketEntity(ticketId).getId();

        DataTablesOutput<UserEntity> all =
                userRepo.findAll(input, findAllUsersSubscribedToTicket(id));

        DataTablesOutput<UserDto> result = new DataTablesOutput<>();

        modelMapper.map(all, result);
        result.setData(modelMapper.map(all.getData(), new TypeToken<List<UserDto>>() {
        }.getType()));

        return result;

    }

    @Override
    public void removeAssignedDev(String ticketId, String userId) {
        TicketEntity ticketEntity = getTicketEntity(ticketId);
        UserDto subscriberEntity = userService.getUserById(userId);

        boolean isRemoved = ticketEntity.removeAssignedDev(
                modelMapper.map(subscriberEntity, UserEntity.class));

        if (!isRemoved)
            throw new ProjectServiceException(NO_RECORD_FOUND, userId);

        ticketRepo.save(ticketEntity);
    }

    @Override
    public void createComment(String ticketId, String creatorId, CommentDto comment) {
        CommentEntity commentEntity = modelMapper.map(comment, CommentEntity.class);

        commentEntity.setPublicId(utils.generateCommentId(10));
        commentEntity.setUploadTime(Date.from(Instant.now()));

        UserEntity creator = modelMapper.map(userService.getUserById(creatorId), UserEntity.class);
        TicketEntity ticketEntity = this.getTicketEntity(ticketId);

        boolean isCreatorAdded = commentEntity.addCreator(creator);
        if (!isCreatorAdded)
            throw new TicketServiceException(RECORD_ALREADY_ADDED, creatorId);

        boolean isTicketAdded = commentEntity.addTicket(ticketEntity);
        if (!isTicketAdded)
            throw new TicketServiceException(RECORD_ALREADY_ADDED, ticketId);

        commentRepo.save(commentEntity);
    }


    @Override
    public Page<CommentDto> getComments(String ticketId,
                                        int page, int limit,
                                        String sortBy, Sort.Direction dir) {
        if (page < 1 || limit < 1) throw new IllegalArgumentException();

        TicketEntity ticketEntity = getTicketEntity(ticketId);

        PageRequest pageRequest =
                PageRequest.of(page - 1, limit, dir, sortBy);


        Page<CommentEntity> commentEntities =
                commentRepo.findAllByTicket(ticketEntity, pageRequest);

        return modelMapper.map(commentEntities, new TypeToken<Page<CommentDto>>() {
        }.getType());
    }
}
