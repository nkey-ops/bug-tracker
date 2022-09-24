package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.security.UserPrincipal;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.shared.dto.ProjectDto;
import com.bluesky.bugtraker.shared.dto.TicketDto;
import com.bluesky.bugtraker.shared.dto.TicketsInfoDTO;
import com.bluesky.bugtraker.shared.dto.UserDto;
import com.bluesky.bugtraker.view.model.rensponse.ProjectResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.TicketResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import com.bluesky.bugtraker.view.model.request.UserRequestModel;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.Set;

@Controller
@RequestMapping("/users")
@SessionAttributes("user")
public class UserController {
    private UserService userService;
    private ModelMapper modelMapper = new ModelMapper();

    public UserController(UserService userService) {
        this.userService = userService;

        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    }

    @ModelAttribute("user")
    public UserResponseModel getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth.getPrincipal().toString().equals("anonymousUser"))
            return new UserResponseModel("Anonymous User");

        UserPrincipal userPrincipal = ((UserPrincipal) auth.getPrincipal());

        return getUser(userPrincipal.getId());
    }

    @PreAuthorize("#id == principal.getId()")
    @GetMapping("/{id}")
    public UserResponseModel getUser(@PathVariable String id) {
        return
                modelMapper.map(
                        userService.getUserById(id), UserResponseModel.class);
    }


    @PreAuthorize("#id == principal.getId()")
    @PutMapping(value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public UserResponseModel updateUser(@PathVariable String id,
                                        @RequestBody UserRequestModel userRequestModel) {

        UserDto userDto =
                modelMapper.map(userRequestModel, UserDto.class);

        return
                modelMapper.map(userService.updateUser(id, userDto),
                        UserResponseModel.class);

    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView createUser(@Valid @ModelAttribute
                                   UserRequestModel userRequestModel,
                                   BindingResult bindingResult) {

        ModelAndView mav = new ModelAndView("register");

        if (bindingResult.hasErrors()) {
            return mav;
        }

        UserDto userDto = userService.createUser(
                modelMapper.map(userRequestModel, UserDto.class));

        return new ModelAndView("redirect:/home");
    }


    @PreAuthorize("#id == principal.id")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }


    @PreAuthorize("#userId == principal.id")
    @GetMapping("/{userId}/reported-bugs")
    public String getReportedBugs(
            @PathVariable String userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "15") int limit) {


        Page<TicketDto> bugs = userService.getReportedTickets(userId, page, limit);
        Set<TicketResponseModel> ticketResponseModels = modelMapper.map(
                bugs.getContent(), new TypeToken<Set<TicketResponseModel>>() {
                }.getType());

        return null;
    }

    @PreAuthorize("#id == principal.id")
    @GetMapping("/{id}/bugs-to-be-fixed")
    public CollectionModel<TicketResponseModel> getWorkingOnBugs(
            @PathVariable String id,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "15") int limit) {


        Page<TicketDto> workingOnBugs = userService.getWorkingOnTickets(id, page, limit);

        Set<TicketResponseModel> ticketResponseModels = modelMapper.map(
                workingOnBugs.getContent(), new TypeToken<Set<TicketResponseModel>>() {
                }.getType());

        return null;
    }

    @PreAuthorize("#id == principal.id")
    @GetMapping("/{id}/subscribed-to-projects")
    public CollectionModel<ProjectResponseModel> getSubscribedOnProjects(
            @PathVariable String id,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "15") int limit) {


        Page<ProjectDto> subscribedOnProjects = userService.getSubscribedOnProjects(id, page, limit);

        Set<ProjectResponseModel> projectResponseModels = modelMapper.map(
                subscribedOnProjects.getContent(), new TypeToken<Set<ProjectResponseModel>>() {
                }.getType());

        return null;
    }

    @PreAuthorize("#userId == principal.id")
    @GetMapping("/{userId}/tickets-info")
    public ResponseEntity<?> getTicketsInfo( @PathVariable String userId) {
        return ResponseEntity.ok(userService.getTicketsInfo(userId));
    }

    @PreAuthorize("#userId == principal.id")
    @GetMapping("/{userId}/info")
    public ResponseEntity<?> getUserInfo( @PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserInfo(userId));
    }


}













