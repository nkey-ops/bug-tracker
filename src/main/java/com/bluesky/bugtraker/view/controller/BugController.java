package com.bluesky.bugtraker.view.controller;


import com.bluesky.bugtraker.security.UserPrincipal;
import com.bluesky.bugtraker.service.BugService;
import com.bluesky.bugtraker.shared.dto.BugDto;
import com.bluesky.bugtraker.view.model.rensponse.BugResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.ProjectResponseModel;
import com.bluesky.bugtraker.view.model.request.BugRequestModel;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/users/{userId}/projects/{projectName}/bugs")
public class BugController {


    private BugService bugService;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public BugController(BugService bugService) {
        this.bugService = bugService;
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    }

    @GetMapping("/{bugId}")
    public BugResponseModel getBug(@PathVariable String userId,
                                   @PathVariable String projectName,
                                   @PathVariable String bugId) {
        BugDto bugDto = bugService.getBugFromProject(userId, projectName, bugId);

        return modelMapper.map(bugDto, BugResponseModel.class);
    }
    @GetMapping
    public Set<BugResponseModel> getBugs(@PathVariable String userId,
                                   @PathVariable String projectName) {
        Set<BugDto> bugDto = bugService.getBugsFromProject(userId, projectName);

        return modelMapper.map(bugDto, new TypeToken<Set<BugResponseModel>>() {}.getType());
    }


    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public BugResponseModel createBug(@PathVariable String userId,
                                      @PathVariable String projectName,
                                      @AuthenticationPrincipal UserPrincipal reporter,
                                      @RequestBody BugRequestModel bug) {

        BugDto bugDto = bugService.createBug(userId, projectName,
                modelMapper.map(bug, BugDto.class), reporter.getId());


        return modelMapper.map(bugDto, BugResponseModel.class);
    }

    @PatchMapping(value = "/{bugId}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public BugResponseModel updateBug(@PathVariable String userId,
                                      @PathVariable String projectName,
                                      @PathVariable String bugId,
                                      @RequestBody BugRequestModel bug) {

        BugDto bugDto = bugService.updateBug(userId, projectName, bugId, modelMapper.map(bug, BugDto.class));


        return modelMapper.map(bugDto, BugResponseModel.class);
    }


    @DeleteMapping("/{bugId}")
    public ResponseEntity<?> deleteBug(@PathVariable String userId,
                                       @PathVariable String projectName,
                                       @PathVariable String bugId) {

        bugService.deleteBug(userId, projectName, bugId);

        return ResponseEntity.noContent().build();

    }
}