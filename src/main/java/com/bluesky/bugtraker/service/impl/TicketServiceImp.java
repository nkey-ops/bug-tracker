package com.bluesky.bugtraker.service.impl;

import com.bluesky.bugtraker.exceptions.serviceexception.TicketServiceException;
import com.bluesky.bugtraker.io.entity.*;
import com.bluesky.bugtraker.io.repository.CommentRepository;
import com.bluesky.bugtraker.io.repository.TicketHistoryRepository;
import com.bluesky.bugtraker.io.repository.TicketRepository;
import com.bluesky.bugtraker.service.ProjectService;
import com.bluesky.bugtraker.service.TicketService;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.service.Utils;
import com.bluesky.bugtraker.shared.dto.*;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.bluesky.bugtraker.exceptions.ErrorMessages.*;
import static com.bluesky.bugtraker.service.specifications.Specs.findAllByProjectId;
import static com.bluesky.bugtraker.service.specifications.Specs.findAllUsersSubscribedToProject;

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
    public void createTicket(String userId, String projectName, TicketDto ticketDto, String reporterId) {
        ProjectEntity projectEntity =
                modelMapper.map(
                        projectService.getProject(projectName),
                        ProjectEntity.class);

        TicketEntity ticketEntity = modelMapper.map(ticketDto, TicketEntity.class);

        ticketEntity.setPublicId(utils.generateBugId(10));
        ticketEntity.setReportedTime(Date.from(Instant.now()));
        ticketEntity.setLastUpdateTime(Date.from(Instant.now()));
        ticketEntity.setHowToSolve("Solution is not found");

        ticketEntity.setReporter(
                modelMapper.map(
                        userService.getUserById(reporterId), UserEntity.class));

        boolean isAdded =  projectEntity.addTicket(ticketEntity);
        if (!isAdded)
            throw new TicketServiceException(RECORD_ALREADY_ADDED, projectEntity.getPublicId());

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
    public void deleteBug(String userId, String projectId, String ticketId) {
        TicketDto ticketDto = getTicket(ticketId);

        projectService.removeTicket(projectId, ticketDto);
    }

    private void createTicketRecord(TicketEntity mainTicketEntity) {
        TicketRecordEntity ticketRecordEntity =
                modelMapper.map(mainTicketEntity, TicketRecordEntity.class);

        ticketRecordEntity.setId(null);
        ticketRecordEntity.setPublicId(utils.generateTicketRecordId(15));
        ticketRecordEntity.setMainTicket(mainTicketEntity);
        
        boolean isAdded = mainTicketEntity.addTicketRecord(ticketRecordEntity);

        if (!isAdded)
            throw new TicketServiceException(RECORD_ALREADY_ADDED, mainTicketEntity.getPublicId());

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
    public void createComment(String ticketId, String creatorId, CommentDto comment) {
        CommentEntity commentEntity = modelMapper.map(comment, CommentEntity.class);

        commentEntity.setPublicId(utils.generateCommentId(10));
        commentEntity.setUploadTime(Date.from(Instant.now()));
        
        UserEntity creator = modelMapper.map(userService.getUserById(creatorId), UserEntity.class);
        TicketEntity ticketEntity = this.getTicketEntity(ticketId);
        
        boolean isCreatorAdded  = commentEntity.addCreator(creator);
        if(!isCreatorAdded)
            throw new TicketServiceException(RECORD_ALREADY_ADDED,  creatorId);
        
        boolean isTicketAdded = commentEntity.addTicket(ticketEntity);
        if(!isTicketAdded)
            throw new TicketServiceException(RECORD_ALREADY_ADDED,  ticketId);

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
