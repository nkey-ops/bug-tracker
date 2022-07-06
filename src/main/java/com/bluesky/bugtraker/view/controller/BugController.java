package com.bluesky.bugtraker.view.controller;


import com.bluesky.bugtraker.security.UserPrincipal;
import com.bluesky.bugtraker.service.BugService;
import com.bluesky.bugtraker.shared.dto.BugDto;
import com.bluesky.bugtraker.view.model.rensponse.BugResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.assembler.BugModelAssembler;
import com.bluesky.bugtraker.view.model.request.BugRequestModel;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/users/{userId}/projects/{projectName}/bugs")
public class BugController {


    private final BugService bugService;
    private  final BugModelAssembler modelAssembler;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public BugController(BugService bugService, BugModelAssembler modelAssembler) {
        this.bugService = bugService;
        this.modelAssembler = modelAssembler;

        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    }
    @PreAuthorize("#userId == principal.id or  principal.isSubscribedTo(#userId, #projectName)")
    @GetMapping("/{bugId}")
    public BugResponseModel getBug(@PathVariable String userId,
                                   @PathVariable String projectName,
                                   @PathVariable String bugId) {
        BugDto bugDto = bugService.getBugFromProject(userId, projectName, bugId);

        return modelAssembler.toModel(modelMapper.map(bugDto, BugResponseModel.class));
    }
    @PreAuthorize("#userId == principal.id or principal.isSubscribedTo(#userId, #projectName)")
    @GetMapping
    public CollectionModel<BugResponseModel> getBugs(@PathVariable String userId,
                                                     @PathVariable String projectName,
                                                     @RequestParam(value = "page", defaultValue = "1") int page,
                                                     @RequestParam(value = "limit", defaultValue = "15") int limit) {

        Set<BugDto> bugDto = bugService.getBugsFromProject(userId, projectName, page, limit);

        Set<BugResponseModel> bugResponseModels =
                modelMapper.map(bugDto, new TypeToken<Set<BugResponseModel>>() {}.getType());

        return modelAssembler.toCollectionModelWithSelfRel(bugResponseModels, userId, projectName);
    }



    @PreAuthorize("#userId == principal.id or principal.isSubscribedTo(#userId, #projectName)")
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public BugResponseModel createBug(@PathVariable String userId,
                                      @PathVariable String projectName,
                                      @AuthenticationPrincipal UserPrincipal reporter,
                                      @RequestBody BugRequestModel bug) {

        BugDto bugDto = bugService.createBug(userId, projectName,
                modelMapper.map(bug, BugDto.class), reporter.getId());


        return modelAssembler.toModel(modelMapper.map(bugDto, BugResponseModel.class));
    }

    @PreAuthorize("#userId == principal.id or principal.isSubscribedTo(#userId, #projectName)")
    @PatchMapping(value = "/{bugId}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public BugResponseModel updateBug(@PathVariable String userId,
                                      @PathVariable String projectName,
                                      @PathVariable String bugId,
                                      @RequestBody BugRequestModel bug) {

        BugDto bugDto = bugService.updateBug(userId, projectName, bugId, modelMapper.map(bug, BugDto.class));


        return modelAssembler.toModel(modelMapper.map(bugDto, BugResponseModel.class));
    }


    @PreAuthorize("#userId == principal.id")
    @DeleteMapping("/{bugId}")
    public ResponseEntity<?> deleteBug(@PathVariable String userId,
                                       @PathVariable String projectName,
                                       @PathVariable String bugId) {

        bugService.deleteBug(userId, projectName, bugId);

        return ResponseEntity.noContent().build();

    }
}