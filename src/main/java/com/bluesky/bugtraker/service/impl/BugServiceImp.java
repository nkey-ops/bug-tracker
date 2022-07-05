package com.bluesky.bugtraker.service.impl;

import com.bluesky.bugtraker.exceptions.serviceexception.BugServiceException;
import com.bluesky.bugtraker.io.entity.BugEntity;
import com.bluesky.bugtraker.io.repository.BugRepository;
import com.bluesky.bugtraker.service.BugService;
import com.bluesky.bugtraker.service.ProjectService;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.shared.Utils;
import com.bluesky.bugtraker.shared.dto.BugDto;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

import static com.bluesky.bugtraker.exceptions.ErrorMessages.NO_RECORD_FOUND;
import static com.bluesky.bugtraker.exceptions.ErrorMessages.RECORD_ALREADY_EXISTS;

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


    private Optional<BugDto> getBugFromProjectOptional(String userId,
                                                       String projectName,
                                                       String bugId) {
        return projectService.getProject(userId, projectName).getBugs()
                .stream()
                .filter(bug -> bug.getPublicId().equals(bugId))
                .findFirst();
    }

    @Override
    public BugDto getBugFromProject(String userId,
                                    String projectName,
                                    String bugId) {

        return getBugFromProjectOptional(userId, projectName, bugId)
                .orElseThrow(() -> new BugServiceException(NO_RECORD_FOUND, bugId));

    }

    @Override
    public Set<BugDto> getBugsFromProject(String userId, String projectName) {
        return projectService.getProject(userId, projectName).getBugs();
    }

    @Override
    public BugDto createBug(String userId, String projectName, BugDto bugDto, String reporterId) {
        if(getBugFromProjectOptional(userId, projectName, bugDto.getPublicId()).isPresent())
                throw new BugServiceException(RECORD_ALREADY_EXISTS, bugDto.getPublicId());

        bugDto.setPublicId(utils.generateBugId(30));
        bugDto.setReportedBy(userService.getUserById(reporterId));
        bugDto.setProject(projectService.getProject(userId, projectName));

        BugEntity bugEntity = modelMapper.map(bugDto, BugEntity.class);

    return  modelMapper.map(bugRepo.save(bugEntity), BugDto.class);
    }

    @Override
    public BugDto updateBug(String userId, String projectName, String bugId, BugDto bugDto) {
        BugDto oldBugDto = getBugFromProject(userId, projectName, bugId);

        modelMapper.map(oldBugDto, bugDto);

        BugEntity savedEntity = bugRepo.save(modelMapper.map(bugDto, BugEntity.class));

        return modelMapper.map(savedEntity, BugDto.class);
    }

    @Override
    public void deleteBug(String userId, String projectName, String bugId) {
        BugDto bugDto = getBugFromProject(userId, projectName, bugId);


        bugRepo.delete(modelMapper.map(bugDto, BugEntity.class));
    }

}
