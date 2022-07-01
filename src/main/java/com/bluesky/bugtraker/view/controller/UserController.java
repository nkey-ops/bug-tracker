package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.shared.dto.UserDto;
import com.bluesky.bugtraker.view.model.rensponse.UserModelAssembler;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import com.bluesky.bugtraker.view.model.request.UserRequestModel;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public EntityModel<UserResponseModel> getUser(@PathVariable String id) {
        UserResponseModel responseModel =
                modelMapper.map(userService.getUserById(id), UserResponseModel.class);

        return  modelAssembler.toModel(responseModel);
    }

    @PutMapping(value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public  EntityModel<UserResponseModel> updateUser(@PathVariable String id,
                                        @RequestBody UserRequestModel userRequestModel) {

        UserDto userDto =
                modelMapper.map(userRequestModel, UserDto.class);

        UserResponseModel responseModel =
                modelMapper.map( userService.updateUser(id, userDto),
                                 UserResponseModel.class);

        return modelAssembler.toModel(responseModel);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    public EntityModel<UserResponseModel> createUser(@RequestBody UserRequestModel userRequestModel) {

        UserDto userDto = userService.createUser(
                modelMapper.map(userRequestModel, UserDto.class));


        return modelAssembler.toModel(
                        modelMapper.map(userDto, UserResponseModel.class));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}













