package com.bluesky.bugtraker.view.controller;


import com.bluesky.bugtraker.service.BugService;
import com.bluesky.bugtraker.shared.dto.BugDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import com.bluesky.bugtraker.view.model.rensponse.BugResponseModel;
import com.bluesky.bugtraker.view.model.request.BugRequestModel;
import com.bluesky.bugtraker.view.model.request.UserRequestModel;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/bugs")
public class BugController {

    private BugService bugService;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public BugController(BugService bugService) {
        this.bugService = bugService;
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    }

    @GetMapping("/{id}")
    public BugResponseModel getBug(@PathVariable String id){
        BugDto bugDto = bugService.getBug(id);

        return  modelMapper.map(bugDto, BugResponseModel.class);
    }


//    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
////    public  BugResponseModel createBug(@RequestParam BugRequestModel bug, @RequestParam UserRequestModel user){
////    //    We need to get the name of submitter
//        BugDto bugDto = bugService.create(
//                                    modelMapper.map(bug, BugDto.class),
//                                    modelMapper.map(user, UserDto.class));
//
//        return  modelMapper.map(bugDto, BugResponseModel.class);
//    }

    @PostMapping(value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public  BugResponseModel createBug(@RequestBody BugRequestModel bug, @PathVariable String id){
    //    We need to get the name of submitter
        BugDto bugDto = bugService.create(
                                    modelMapper.map(bug, BugDto.class), id);
//                                    modelMapper.map(user, UserDto.class));

        return  modelMapper.map(bugDto, BugResponseModel.class);
    }
}
