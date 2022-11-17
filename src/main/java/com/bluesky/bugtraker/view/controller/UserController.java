package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.security.UserPrincipal;
import com.bluesky.bugtraker.service.ProjectService;
import com.bluesky.bugtraker.service.TicketService;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.shared.dto.ProjectDTO;
import com.bluesky.bugtraker.shared.dto.TicketDTO;
import com.bluesky.bugtraker.shared.dto.UserDTO;
import com.bluesky.bugtraker.view.model.rensponse.ProjectResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.TicketResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.assambler.ProjectModelAssembler;
import com.bluesky.bugtraker.view.model.rensponse.assambler.TicketModelAssembler;
import com.bluesky.bugtraker.view.model.rensponse.assambler.UserModelAssembler;
import com.bluesky.bugtraker.view.model.request.UserRegisterModel;
import com.bluesky.bugtraker.view.model.request.UserRequestModel;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
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

@Controller
@RequestMapping("/users")
@SessionAttributes("user")
public class UserController {
    private final UserService userService;
    private final TicketService ticketService;
    private final ProjectService projectService;
    
    private final ModelMapper modelMapper;
    private final ProjectModelAssembler projectModelAssembler;
    private final TicketModelAssembler ticketModelAssembler;
    private final UserModelAssembler userModelAssembler;

    public UserController(UserService userService, 
                          TicketService ticketService, 
                          ProjectService projectService, 
                          ModelMapper modelMapper, 
                          ProjectModelAssembler projectModelAssembler, 
                          TicketModelAssembler ticketModelAssembler, 
                          UserModelAssembler userModelAssembler) {
        
        this.userService = userService;
        this.ticketService = ticketService;
        this.projectService = projectService;
        this.modelMapper = modelMapper;
        this.projectModelAssembler = projectModelAssembler;
        this.ticketModelAssembler = ticketModelAssembler;
        this.userModelAssembler = userModelAssembler;
    }

    @ModelAttribute("user")
    public UserResponseModel getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth.getPrincipal().toString().equals("anonymousUser"))
            return new UserResponseModel("Anonymous User");

        UserPrincipal userPrincipal = ((UserPrincipal) auth.getPrincipal());
        UserResponseModel userResponseModel = (UserResponseModel) getUser(userPrincipal.getId()).getBody();
        assert userResponseModel != null;

        return userModelAssembler.toModel(userResponseModel);
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView createUser(@Valid @ModelAttribute("userRegisterModel")
                                   UserRegisterModel userRegisterModel,
                                   BindingResult bindingResult) {

        ModelAndView mav = new ModelAndView("register");

        if (bindingResult.hasErrors()) {
            return mav;
        }

        userService.createUser(modelMapper.map(userRegisterModel, UserDTO.class));

        return new ModelAndView("redirect:/home");
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable String userId) {

        UserDTO userById = userService.getUserById(userId);
        UserResponseModel userResponseModel = modelMapper.map(userById, UserResponseModel.class);

        return ResponseEntity.ok(userResponseModel);
    }

    @PreAuthorize("#userId == principal.id")
    @GetMapping("/{userId}/info")
    public ResponseEntity<?> getUserInfo(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserInfo(userId));
    }


    @PreAuthorize("(#userId == principal.getId() and #userRequestModel.getRole() == null) or " +
            "hasRole('ROLE_SUPER_ADMIN')")
    @PatchMapping(value = "/{userId}",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> updateUser(@PathVariable String userId,
                                        @Valid @ModelAttribute("userRequestModel")
                                        UserRequestModel userRequestModel) {

        UserDTO userDto = modelMapper.map(userRequestModel, UserDTO.class);
        userService.updateUser(userId, userDto);

        return ResponseEntity.ok().build();
    }


    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<DataTablesOutput<UserResponseModel>> getUsers(@Valid DataTablesInput input) {
        DataTablesOutput<UserDTO> userDTOs = userService.getUsers(input);

        return ResponseEntity.ok(userModelAssembler.toDataTablesOutputModel(userDTOs));
    }

    @PreAuthorize("#userId == principal.id or " +
            "(hasRole('ADMIN') and !@userServiceImp.isSuperAdmin(#userId)) or " +
            "hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);

        return ResponseEntity.ok().build();
    }


    @PreAuthorize("#userId == principal.id")
    @GetMapping("/{userId}/tickets-subscriptions")
    public ResponseEntity<DataTablesOutput<TicketResponseModel>> getSubscribedToTickets(
            @PathVariable String userId,
            @Valid DataTablesInput input) {

        DataTablesOutput<TicketDTO> workingOnBugs = ticketService.getTicketsUserSubscribedTo(userId, input);
        DataTablesOutput<TicketResponseModel> assembledTickets = ticketModelAssembler.toDataTablesOutputModel(workingOnBugs);

        return ResponseEntity.ok(assembledTickets);
    }

    @PreAuthorize("#userId == principal.id")
    @GetMapping("/{userId}/project-subscriptions")
    @ResponseBody
    public ResponseEntity<DataTablesOutput<ProjectResponseModel>> getSubscribedToProjects(
            @PathVariable String userId,
            @Valid DataTablesInput input) {

        DataTablesOutput<ProjectDTO> subscribedOnProjects = projectService.getSubscribedToProjects(userId, input);
        DataTablesOutput<ProjectResponseModel> assembledProjects =
                projectModelAssembler.toDataTablesOutputModel(subscribedOnProjects);

        return ResponseEntity.ok(assembledProjects);
    }

    @PreAuthorize("#userId == principal.id")
    @GetMapping("/{userId}/tickets-info")
    public ResponseEntity<?> getTicketsInfo(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getProjectsInfo(userId));
    }


}













