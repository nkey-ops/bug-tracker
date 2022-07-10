package com.bluesky.bugtraker.service.impl;

import com.bluesky.bugtraker.exceptions.serviceexception.UserServiceException;
import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.entity.authorization.RoleEntity;
import com.bluesky.bugtraker.io.repository.RoleRepository;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.security.UserPrincipal;
import com.bluesky.bugtraker.service.BugService;
import com.bluesky.bugtraker.service.ProjectService;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.shared.Utils;
import com.bluesky.bugtraker.shared.authorizationenum.Role;
import com.bluesky.bugtraker.shared.dto.ProjectDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static com.bluesky.bugtraker.exceptions.ErrorMessages.NO_RECORD_FOUND;
import static com.bluesky.bugtraker.exceptions.ErrorMessages.RECORD_ALREADY_EXISTS;

@Service
public class UserServiceImp implements UserService {
    //TODO make a constant values for user id length;
    private UserRepository userRepo;
    private RoleRepository roleRepo;

    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private Utils utils;
    private ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public UserServiceImp(UserRepository userRepo, RoleRepository roleRepo,
                          BCryptPasswordEncoder bCryptPasswordEncoder, Utils utils) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;


        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.utils = utils;
    }


    private UserEntity getUserEntityByPublicId(String id){
        return userRepo.findByPublicId(id)
                .orElseThrow(() -> new UserServiceException(NO_RECORD_FOUND, id));
    }

    @Override
    public UserDto getUserById(String id) {
        UserEntity userEntity = getUserEntityByPublicId(id);
        return modelMapper.map(userEntity, UserDto.class);
    }
    @Transactional
    @Override
    public UserDto getUserByEmail(String email) {
        UserEntity userEntity = userRepo.findByEmail(email).orElseThrow(() -> new UserServiceException(NO_RECORD_FOUND, email));

        return modelMapper.map(userEntity, UserDto.class);
    }

    @Override
    public UserDto createAdminUser(String email, String password, Set<RoleEntity> roles) {
        if (userRepo.existsByEmail(email)) throw new UserServiceException(RECORD_ALREADY_EXISTS, email);
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(email);
        userEntity.setUserName("Admiral");
        userEntity.setPublicId(utils.generateUserId(30));
        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(password));
//        TODO after testing change to false;
        userEntity.setEmailVerificationStatus(true);
        userEntity.setRoles(roles);

        return modelMapper.map(userRepo.save(userEntity), UserDto.class);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userRepo.existsByEmail(userDto.getEmail()))
            throw new UserServiceException(RECORD_ALREADY_EXISTS, userDto.getEmail());

        UserEntity userEntity = modelMapper.map(userDto, UserEntity.class);
        userEntity.setPublicId(utils.generateUserId(30));
        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));
        userEntity.setEmailVerificationToken("mock");
        //        TODO after testing change to false;
        userEntity.setEmailVerificationStatus(true);


        userEntity.setRoles(Set.of(
                roleRepo.findByRole((Role.ROLE_USER)).get()));

        UserEntity savedEntity = userRepo.save(userEntity);

        return modelMapper.map(savedEntity, UserDto.class);
    }

    @Override
    public UserDto updateUser(String id, UserDto userDto) {
        UserEntity userEntity = getUserEntityByPublicId(id);
        userEntity.setUserName(userDto.getUserName());

        return modelMapper.map(userRepo.save(userEntity), UserDto.class);
    }

    @Transactional
    @Override
    public void deleteUser(String id) {
        UserEntity userEntity = getUserEntityByPublicId(id);

//        userEntity.getSubscribedToProjects()
//                .forEach(project ->
//                        projectService.removeSubscriber(
//                                project.getCreator().getPublicId(),
//                                project.getName(),
//                                userEntity.getPublicId()
//                        )
//                );


        userRepo.delete(userEntity);
    }

//    @Override
//    public void addProjectToUser(String userId, ProjectDto projectDto) {
//        UserEntity userEntity = getUserEntityByPublicId(userId);
//        ProjectEntity projectEntity = modelMapper.map(projectDto, ProjectEntity.class);
//
//        boolean isAdded = userEntity.addProject(projectEntity);
//        if (!isAdded)
//            throw new UserServiceException(RECORD_ALREADY_EXISTS, projectDto.getName());
//
//        userRepo.save(userEntity);
//    }
//
//    @Override
//    public void removeProject(String userId, ProjectDto projectDto) {
//        UserEntity userEntity = getUserEntityByPublicId(userId);
//        ProjectEntity projectEntity = modelMapper.map(projectDto, ProjectEntity.class);
//
//        boolean isRemoved = userEntity.removeProject(projectEntity);
//
//        if (!isRemoved)
//            throw new UserServiceException(NO_RECORD_FOUND, projectDto.getName());
//
//        userRepo.save(userEntity);
//    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = userRepo.findByEmail(email).orElseThrow(() -> new UserServiceException(NO_RECORD_FOUND, email));

        return new UserPrincipal(userEntity);
    }
}
