package com.bluesky.bugtraker.service.impl;

import com.bluesky.bugtraker.exceptions.serviceexception.ProjectServiceException;
import com.bluesky.bugtraker.exceptions.serviceexception.TicketServiceException;
import com.bluesky.bugtraker.io.entity.*;
import com.bluesky.bugtraker.io.repository.CommentRepository;
import com.bluesky.bugtraker.io.repository.TicketRecordsRepository;
import com.bluesky.bugtraker.io.repository.TicketRepository;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.service.TicketService;
import com.bluesky.bugtraker.service.utils.DataExtractionUtils;
import com.bluesky.bugtraker.service.utils.Utils;
import com.bluesky.bugtraker.shared.dto.CommentDTO;
import com.bluesky.bugtraker.shared.dto.TicketDTO;
import com.bluesky.bugtraker.shared.dto.TicketRecordDTO;
import com.bluesky.bugtraker.shared.dto.UserDTO;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Date;

import static com.bluesky.bugtraker.exceptions.ErrorType.NO_RECORD_FOUND;
import static com.bluesky.bugtraker.exceptions.ErrorType.RECORD_ALREADY_ADDED;
import static com.bluesky.bugtraker.service.specifications.Specs.*;

@Service
public class TicketServiceImp implements TicketService {
    private final TicketRepository ticketRepo;
    private final TicketRecordsRepository ticketRecordRepo;
    private final CommentRepository commentRepo;
    private final UserRepository userRepo;
    private final DataExtractionUtils dataExtractionUtils;
    private final Utils utils;
    private final ModelMapper modelMapper;

    @Autowired
    public TicketServiceImp(TicketRepository ticketRepo,
                            TicketRecordsRepository ticketRecordRepo,
                            CommentRepository commentRepo,
                            UserRepository userRepo,
                            DataExtractionUtils dataExtractionUtils,
                            Utils utils,
                            ModelMapper modelMapper) {
        this.ticketRepo = ticketRepo;
        this.ticketRecordRepo = ticketRecordRepo;
        this.commentRepo = commentRepo;
        this.userRepo = userRepo;
        this.dataExtractionUtils = dataExtractionUtils;
        this.utils = utils;

        this.modelMapper = modelMapper;
    }

    @Override
    @NotNull
    public TicketDTO getTicket(@NotNull String ticketId) {
        TicketEntity ticketEntity = dataExtractionUtils.getTicketEntity(ticketId);

        return modelMapper.map(ticketEntity, TicketDTO.class);
    }

    @Override
    @NotNull
    public DataTablesOutput<TicketDTO> getTickets(@NotNull String projectId, @NotNull DataTablesInput input) {
        ProjectEntity projectEntity = dataExtractionUtils.getProjectEntity(projectId);

        DataTablesOutput<TicketEntity> projectTickets =
                ticketRepo.findAll(input, ticketByProject(projectEntity));
        return utils.map(projectTickets, new TypeToken<>() {
        });
    }

    @Override
    public TicketDTO createTicket(@NotNull String projectId, @NotNull TicketDTO ticketDto, @NotNull String reporterId) {
        ProjectEntity projectEntity = dataExtractionUtils.getProjectEntity(projectId);
        TicketEntity ticketEntity = modelMapper.map(ticketDto, TicketEntity.class);

        ticketEntity.setPublicId(utils.generateRandomString(10));
        ticketEntity.setCreatedTime(Date.from(Instant.now()));
        ticketEntity.setLastUpdateTime(Date.from(Instant.now()));
        ticketEntity.setHowToSolve("Solution is not found");

        ticketEntity.setReporter(dataExtractionUtils.getUserEntity(reporterId));

        boolean isAdded = projectEntity.addTicket(ticketEntity);
        if (!isAdded)
            throw new TicketServiceException(RECORD_ALREADY_ADDED, projectEntity.getPublicId());

        TicketEntity savedTicketEntity = ticketRepo.save(ticketEntity);

        createTicketRecord(savedTicketEntity.getPublicId(), reporterId);
        
        return modelMapper.map(savedTicketEntity, TicketDTO.class) ;
    }


    @Override
    public void updateTicket(@NotNull String ticketId, @NotNull TicketDTO ticketDTOUpdates, @NotNull String updatedByUserWithId) {
        TicketEntity ticketEntity = dataExtractionUtils.getTicketEntity(ticketId);

        modelMapper.map(ticketDTOUpdates, ticketEntity);
        ticketEntity.setLastUpdateTime(Date.from(Instant.now()));

        TicketEntity savedTicketEntity = ticketRepo.save(ticketEntity);

        createTicketRecord(savedTicketEntity.getPublicId(), updatedByUserWithId);
    }

    @Override
    public void deleteTicket(@NotNull String ticketId) {
        TicketEntity ticketEntity = dataExtractionUtils.getTicketEntity(ticketId);

        boolean isRemoved = ticketEntity.getProject().removeTicket(ticketEntity);

        if (!isRemoved)
            throw new ProjectServiceException(NO_RECORD_FOUND, ticketId);

        ticketRepo.delete(ticketEntity);
    }

    public void createTicketRecord(@NotNull String mainTicketEntityId, @NotNull String creatorId) {
        TicketEntity mainTicketEntity =
                dataExtractionUtils.getTicketEntity(mainTicketEntityId);
        TicketRecordEntity ticketRecordEntity =
                modelMapper.map(mainTicketEntity, TicketRecordEntity.class);

        ticketRecordEntity.setId(null);
        ticketRecordEntity.setPublicId(utils.generateRandomString(15));
        ticketRecordEntity.setCreatedTime(Date.from(Instant.now()));
        ticketRecordEntity.setMainTicket(mainTicketEntity);
        ticketRecordEntity.setCreator(dataExtractionUtils.getUserEntity(creatorId));

        boolean isAdded = mainTicketEntity.addTicketRecord(ticketRecordEntity);
        if (!isAdded)
            throw new TicketServiceException(RECORD_ALREADY_ADDED, mainTicketEntity.getPublicId());

        ticketRecordRepo.save(ticketRecordEntity);
    }

    @Override
    @NotNull
    public DataTablesOutput<TicketRecordDTO> getTicketRecords(@NotNull String ticketId, @NotNull DataTablesInput input) {
        TicketEntity ticketEntity = dataExtractionUtils.getTicketEntity(ticketId);

        DataTablesOutput<TicketRecordEntity> ticketRecords =
                ticketRecordRepo.findAll(input, allTicketRecordsByTicket(ticketEntity));
        return utils.map(ticketRecords, new TypeToken<>() {
        });
    }


    @Override
    @NotNull
    public TicketRecordDTO getTicketRecord(@NotNull String recordId) {
        TicketRecordEntity ticketRecordEntity = ticketRecordRepo.findByPublicId(recordId);
        return modelMapper.map(ticketRecordEntity, TicketRecordDTO.class);
    }

    @Override
    public void addSubscriber(String ticketId, String userId) {
        TicketEntity ticketEntity = dataExtractionUtils.getTicketEntity(ticketId);
        UserEntity userEntity = dataExtractionUtils.getUserEntity(userId);

        boolean isAdded = ticketEntity.addSubscriber(userEntity);

        if (!isAdded)
            throw new ProjectServiceException(RECORD_ALREADY_ADDED, ticketId);
        else
            ticketRepo.save(ticketEntity);
    }

    @Override
    public DataTablesOutput<UserDTO> getSubscribers(String ticketId, DataTablesInput input) {
        TicketEntity ticketEntity = dataExtractionUtils.getTicketEntity(ticketId);

        DataTablesOutput<UserEntity> subscribersEntities =
                userRepo.findAll(input, ticketSubscribersByTicket(ticketEntity));
        return utils.map(subscribersEntities, new TypeToken<>() {
        });
    }

    @Override
    public void removeSubscriber(String ticketId, String userId) {
        TicketEntity ticketEntity = dataExtractionUtils.getTicketEntity(ticketId);
        UserEntity subscriberEntity = dataExtractionUtils.getUserEntity(userId);

        boolean isRemoved = ticketEntity.removeSubscriber(subscriberEntity);

        if (!isRemoved)
            throw new ProjectServiceException(NO_RECORD_FOUND, userId);
        else
            ticketRepo.save(ticketEntity);
    }

    @Override
    public void createComment(String ticketId, String commentCreatorId, CommentDTO comment) {
        CommentEntity commentEntity = modelMapper.map(comment, CommentEntity.class);
        commentEntity.setPublicId(utils.generateRandomString(10));
        commentEntity.setUploadTime(Date.from(Instant.now()));

        UserEntity creator = dataExtractionUtils.getUserEntity(commentCreatorId);
        TicketEntity ticketEntity = dataExtractionUtils.getTicketEntity(ticketId);

        boolean isCreatorAdded = commentEntity.setCreator(creator);
        if (!isCreatorAdded)
            throw new TicketServiceException(RECORD_ALREADY_ADDED, commentCreatorId);

        boolean isTicketAdded = commentEntity.addTicket(ticketEntity);
        if (!isTicketAdded)
            throw new TicketServiceException(RECORD_ALREADY_ADDED, ticketId);

        commentRepo.save(commentEntity);
    }


    @Override
    public Page<CommentDTO> getComments(String ticketId,
                                        int page, int limit,
                                        String sortBy, Sort.Direction dir) {
        if (page < 1 || limit < 1) throw new IllegalArgumentException();

        TicketEntity ticketEntity = dataExtractionUtils.getTicketEntity(ticketId);

        PageRequest pageRequest =
                PageRequest.of(page - 1, limit, dir, sortBy);


        Page<CommentEntity> commentEntities =
                commentRepo.findAllByTicket(ticketEntity, pageRequest);

        return modelMapper.map(commentEntities, new TypeToken<Page<CommentDTO>>() {
        }.getType());
    }

    @Override
    public DataTablesOutput<TicketDTO> getTicketsUserSubscribedTo(String userId, DataTablesInput input) {
        UserEntity userEntity = dataExtractionUtils.getUserEntity(userId);

        DataTablesOutput<TicketEntity> subscribedToTickets =
                ticketRepo.findAll(input, ticketsUserSubscribedTo(userEntity));
        return utils.map(subscribedToTickets, new TypeToken<>() {
        });
    }


}
