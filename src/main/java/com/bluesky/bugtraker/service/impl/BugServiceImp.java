package com.bluesky.bugtraker.service.impl;

import com.bluesky.bugtraker.exceptions.serviceexception.BugServiceException;
import com.bluesky.bugtraker.io.entity.BugEntity;
import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.repository.BugRepository;
import com.bluesky.bugtraker.service.BugService;
import com.bluesky.bugtraker.service.ProjectService;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.shared.Utils;
import com.bluesky.bugtraker.shared.dto.BugDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.bluesky.bugtraker.exceptions.ErrorMessages.*;

@Service
public class BugServiceImp implements BugService {
    private BugRepository bugRepo;
    private UserService userService;
    private ProjectService projectService;
    private Utils utils;

    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public BugServiceImp(BugRepository bugRepo, UserService userService,
                         Utils utils, ProjectService projectService) {
        this.bugRepo = bugRepo;
        this.userService = userService;
        this.utils = utils;
        this.projectService = projectService;

        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    }

//  TODO make bugId unique in bounds of the project  and change this method
    private BugEntity getBugEntity(String userId,
                                   String projectName,
                                   String bugId) {

        return bugRepo.findByPublicId(bugId)
                .orElseThrow(() -> new BugServiceException(NO_RECORD_FOUND, bugId));
    }

    @Override
    public BugDto getBug(String userId,
                         String projectName,
                         String bugId) {

        BugEntity bugEntity = getBugEntity(userId, projectName, bugId);

        return  modelMapper.map(bugEntity, BugDto.class);
    }

    @Override
    public Set<BugDto> getBugs(String userId, String projectName, int page, int limit) {
        if (page-- < 0 || limit < 1) throw new IllegalArgumentException();

        ProjectEntity projectEntity = modelMapper.map(projectService.getProject(userId, projectName),
                ProjectEntity.class);

        Page<BugEntity> entityPages = bugRepo.findAllByProject(projectEntity, PageRequest.of(page, limit));
        List<BugEntity> content = entityPages.getContent();
        return modelMapper.map(content, new TypeToken<Set<BugDto>>() {
        }.getType());

    }

    @Override
    public BugDto createBug(String userId, String projectName, BugDto bugDto, String reporterId) {
        ProjectEntity projectEntity =
                modelMapper.map(
                        projectService.getProject(userId, projectName),
                        ProjectEntity.class);

        if(bugRepo.existsByProjectAndPublicId(projectEntity,bugDto.getPublicId()))
                throw new BugServiceException(RECORD_ALREADY_EXISTS, bugDto.getPublicId());


        bugDto.setPublicId(utils.generateBugId(10));
        bugDto.setReporter(userService.getUserById(reporterId));

        projectService.addBug(userId, projectName, bugDto);

        return getBug(userId, projectName, bugDto.getPublicId());
    }


    @Override
    public BugDto updateBug(String userId, String projectName, String bugId, BugDto bugDtoUpdates) {
        BugDto bugDto = getBug(userId, projectName, bugId);

        modelMapper.map(bugDtoUpdates, bugDto);

        BugEntity savedEntity = bugRepo.save(
                modelMapper.map(bugDto, BugEntity.class));


        return modelMapper.map(savedEntity, BugDto.class);
    }

    @Override
    public void deleteBug(String userId, String projectName, String bugId) {
        BugDto bugDto = getBug(userId, projectName, bugId);

        projectService.removeBug(userId, projectName, bugDto);
    }

    @Override
    public void addBugFixer(String userId, String projectName, String bugId, String fixerId) {
        BugEntity bugEntity = getBugEntity(userId, projectName, bugId);

        UserDto fixerDto = userService.getUserById(fixerId);
        boolean isAdded = bugEntity.addBugFixer(
                modelMapper.map(fixerDto, UserEntity.class));

        if(!isAdded)
            throw new BugServiceException(RECORD_ALREADY_ADDED, fixerId);

        bugRepo.save(bugEntity);
    }

    @Override
    public void removeBugFixer(String userId, String projectName, String bugId, String fixerId) {
        BugEntity bugEntity = getBugEntity(userId, projectName, bugId);

        UserDto fixerDto = userService.getUserById(fixerId);
        boolean isRemoved = bugEntity.removeBugFixer(
                modelMapper.map(fixerDto, UserEntity.class));

        if(!isRemoved)
            throw new BugServiceException(NO_RECORD_FOUND, fixerId);

        bugRepo.save(bugEntity);
    }

    @Override
    public Set<UserDto> getBugFixers(String userId, String projectName, String bugId,
                                     int page, int limit) {
        if (page < 1 || limit < 1) throw new IllegalArgumentException();

        Set<UserDto> bugFixers = getBug(userId, projectName, bugId).getBugFixers();

        PageImpl<UserDto> pagedUsers =
                new PageImpl<>( bugFixers.stream().toList(),
                                Pageable.ofSize(page), limit);

        return new LinkedHashSet<>(pagedUsers.getContent());
    }
}
