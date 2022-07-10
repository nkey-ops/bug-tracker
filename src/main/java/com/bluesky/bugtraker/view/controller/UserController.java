package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.shared.dto.UserDto;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.assembler.UserModelAssembler;
import com.bluesky.bugtraker.view.model.request.UserRequestModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    @PostMapping
    public UserResponseModel createUser(@RequestBody UserRequestModel userRequestModel) {

        UserDto userDto = userService.createUser(
                modelMapper.map(userRequestModel, UserDto.class));


        return modelAssembler.toModel(
                modelMapper.map(userDto, UserResponseModel.class));
    }


    @PreAuthorize("#id == principal.id")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }


}













