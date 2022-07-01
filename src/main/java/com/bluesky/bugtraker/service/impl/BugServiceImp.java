package com.bluesky.bugtraker.service.impl;

import com.bluesky.bugtraker.exceptions.serviceexception.BugServiceException;
import com.bluesky.bugtraker.io.entity.BugEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.repository.BugRepository;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.service.BugService;
import com.bluesky.bugtraker.shared.Utils;
import com.bluesky.bugtraker.shared.dto.BugDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.bluesky.bugtraker.exceptions.ErrorMessages.NO_RECORD_FOUND;
import static com.bluesky.bugtraker.exceptions.ErrorMessages.RECORD_ALREADY_EXISTS;

@Service
public class BugServiceImp implements BugService {
    private BugRepository bugRepo;
    private Utils utils;
    private UserRepository userRepository;

    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public BugServiceImp(BugRepository bugRepo, Utils utils, UserRepository userRepository) {
        this.bugRepo = bugRepo;
        this.utils = utils;
        this.userRepository = userRepository;
    }

    @Override
    public BugDto getBug(String id) {
        BugEntity bugEntity = bugRepo.findByPublicId(id)
                .orElseThrow(() -> new BugServiceException(NO_RECORD_FOUND, id));

        return modelMapper.map(bugEntity, BugDto.class);
    }

    @Override
    public BugDto create(BugDto bugDto, UserDto reporter) {
        if(bugRepo.findByPublicId(bugDto.getPublicId()).isPresent())
            throw new BugServiceException(RECORD_ALREADY_EXISTS, bugDto.getPublicId());
        if(reporter == null)
            throw new IllegalArgumentException("Reporter cannot be null");

        bugDto.setReportedBy(reporter);

        BugEntity bugEntity = modelMapper.map(bugDto, BugEntity.class);


        return  modelMapper.map(bugRepo.save(bugEntity), BugDto.class);
    }

    @Override
    public BugDto create(BugDto bugDto, String userId) {
        if(bugRepo.findByPublicId(bugDto.getPublicId()).isPresent() )
            throw new BugServiceException(RECORD_ALREADY_EXISTS, bugDto.getPublicId());


        BugEntity bugEntity = modelMapper.map(bugDto, BugEntity.class);

        UserEntity reporter = userRepository.findByPublicId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Reporter cannot be null"));

        bugEntity.setPublicId(utils.generateBugId(30));
        bugEntity.setReportedBy(reporter);



        return  modelMapper.map(bugRepo.save(bugEntity), BugDto.class);
    }



}
