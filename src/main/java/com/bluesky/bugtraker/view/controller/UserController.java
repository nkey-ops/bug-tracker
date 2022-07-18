package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.exceptions.serviceexception.UserServiceException;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.shared.dto.BugDto;
import com.bluesky.bugtraker.shared.dto.ProjectDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import com.bluesky.bugtraker.view.model.rensponse.BugResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.ProjectResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.assembler.BugModelAssembler;
import com.bluesky.bugtraker.view.model.rensponse.assembler.ProjectModelAssembler;
import com.bluesky.bugtraker.view.model.rensponse.assembler.UserModelAssembler;
import com.bluesky.bugtraker.view.model.request.UserRequestModel;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Controller
@RequestMapping("/users")
public class UserController {
    private UserService userService;
    private ModelMapper modelMapper = new ModelMapper();
    private UserModelAssembler modelAssembler;

    public UserController(UserService userService, UserModelAssembler modelAssembler) {
        this.userService = userService;
        this.modelAssembler = modelAssembler;

        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    }

    @PreAuthorize("#id == principal.getId()")
    @GetMapping("/{id}")
    public UserResponseModel getUser(@PathVariable String id) {
        UserResponseModel responseModel =
                modelMapper.map(userService.getUserById(id), UserResponseModel.class);

        return modelAssembler.toModel(responseModel);
    }


    @PreAuthorize("#id == principal.getId()")
    @PutMapping(value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public UserResponseModel updateUser(@PathVariable String id,
                                        @RequestBody UserRequestModel userRequestModel) {

        UserDto userDto =
                modelMapper.map(userRequestModel, UserDto.class);

        UserResponseModel responseModel =
                modelMapper.map(userService.updateUser(id, userDto),
                        UserResponseModel.class);

        return modelAssembler.toModel(responseModel);
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView createUser(@ModelAttribute("user") @Valid
                                   UserRequestModel userRequestModel,
                                   BindingResult bindingResult) {
        ModelAndView mav = new ModelAndView("register");

        if (bindingResult.hasErrors()) {
            return  mav.addObject("userRequest", userRequestModel);
        }


        UserDto  userDto = userService.createUser(
                    modelMapper.map(userRequestModel, UserDto.class));
        UserResponseModel responseModel = modelAssembler.toModel(
                modelMapper.map(userDto, UserResponseModel.class));


        return new ModelAndView("redirect:/home", "userResponse", responseModel);
    }


    @PreAuthorize("#id == principal.id")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }


    @PreAuthorize("#id == principal.id")
    @GetMapping("/{id}/reported-bugs")
    public CollectionModel<BugResponseModel> getReportedBugs(
            @PathVariable String id,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "15") int limit) {


        Set<BugDto> bugs = userService.getReportedBugs(id, page, limit);
        Set<BugResponseModel> bugResponseModels = modelMapper.map(
                bugs, new TypeToken<Set<BugResponseModel>>() {
                }.getType());

        BugModelAssembler bugModelAssembler = new BugModelAssembler();

        return bugModelAssembler.toCollectionModel(bugResponseModels)
                .add(linkTo(methodOn(UserController.class)
                        .getReportedBugs(id, page, limit)).withSelfRel());
    }

    @PreAuthorize("#id == principal.id")
    @GetMapping("/{id}/bugs-to-be-fixed")
    public CollectionModel<BugResponseModel> getWorkingOnBugs(
            @PathVariable String id,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "15") int limit) {


        Set<BugDto> workingOnBugs = userService.getGetWorkingOnBugs(id, page, limit);

        Set<BugResponseModel> bugResponseModels = modelMapper.map(
                workingOnBugs, new TypeToken<Set<BugResponseModel>>() {
                }.getType());

        BugModelAssembler bugModelAssembler = new BugModelAssembler();

        return bugModelAssembler.toCollectionModel(bugResponseModels)
                .add(linkTo(methodOn(UserController.class)
                        .getWorkingOnBugs(id, page, limit)).withSelfRel());

    }

    @PreAuthorize("#id == principal.id")
    @GetMapping("/{id}/subscribed-to-projects")
    public CollectionModel<ProjectResponseModel> getSubscribedProjects(
            @PathVariable String id,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "15") int limit) {


        Set<ProjectDto> subscribedProjects = userService.getSubscribedProjects(id, page, limit);

        Set<ProjectResponseModel> projectResponseModels = modelMapper.map(
                subscribedProjects, new TypeToken<Set<ProjectResponseModel>>() {
                }.getType());


        ProjectModelAssembler projectModelAssembler = new ProjectModelAssembler();

        return projectModelAssembler.toCollectionModel(projectResponseModels)
                .add(linkTo(methodOn(UserController.class)
                        .getSubscribedProjects(id, page, limit)).withSelfRel());

    }


}













