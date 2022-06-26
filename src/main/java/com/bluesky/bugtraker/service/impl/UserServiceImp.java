package com.bluesky.bugtraker.service.impl;

import com.bluesky.bugtraker.exceptions.UserAlreadyExistsException;
import com.bluesky.bugtraker.exceptions.UserNotFoundException;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.shared.UserDto;
import com.bluesky.bugtraker.shared.Utils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImp implements UserService {
    private UserRepository userRepo;
    private Utils utils;
//    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public UserServiceImp(UserRepository userRepo, Utils utils) {
        this.userRepo = userRepo;
        this.utils = utils;
    }

    @Override
    public UserDto getUser(String id) {
        UserEntity userEntity = userRepo.findByPublicId(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return modelMapper.map(userEntity, UserDto.class);
    }

    @Override
    public UserDto createUser(UserDto userDto) {

        if (userRepo.existsByEmail(userDto.getEmail()))
            throw new UserAlreadyExistsException(userDto.getEmail());

        UserEntity userEntity = modelMapper.map(userDto, UserEntity.class);
        userEntity.setPublicId(utils.generateUserId(30));
//        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));
        userEntity.setEncryptedPassword("mock");
        userEntity.setEmailVerificationToken("mock");

        UserEntity savedEntity = userRepo.save(userEntity);

        return modelMapper.map(savedEntity, UserDto.class);
    }

    @Override
    public UserDto updateUser(String id, UserDto userDto) {
        UserEntity userEntity = userRepo.findByPublicId(id).
                orElseThrow(() -> new UserNotFoundException(id));

        userEntity.setUserName(userDto.getUserName());

        return modelMapper.map(userRepo.save(userEntity), UserDto.class);
    }

    @Override
    public void deleteUser(String id) {
        UserEntity userEntity = userRepo.findByPublicId(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userRepo.delete(userEntity);
    }





}
