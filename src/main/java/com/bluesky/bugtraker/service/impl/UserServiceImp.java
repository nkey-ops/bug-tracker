package com.bluesky.bugtraker.service.impl;

import com.bluesky.bugtraker.exceptions.serviceexception.UserServiceException;
import com.bluesky.bugtraker.io.entity.BugEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.entity.authorization.RoleEntity;
import com.bluesky.bugtraker.io.repository.BugRepository;
import com.bluesky.bugtraker.io.repository.RoleRepository;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.security.UserPrincipal;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.shared.Utils;
import com.bluesky.bugtraker.shared.authorizationenum.Role;
import com.bluesky.bugtraker.shared.dto.BugDto;
import com.bluesky.bugtraker.shared.dto.ProjectDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.bluesky.bugtraker.exceptions.ErrorMessages.NO_RECORD_FOUND;
import static com.bluesky.bugtraker.exceptions.ErrorMessages.RECORD_ALREADY_EXISTS;

@Service
public class UserServiceImp implements UserService {
    //TODO make a constant values for user id length;
    private UserRepository userRepo;
    private RoleRepository roleRepo;
    private BugRepository bugRepo;

    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private Utils utils;
    private ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public UserServiceImp(UserRepository userRepo, RoleRepository roleRepo,
                          BugRepository bugRepo,
                          BCryptPasswordEncoder bCryptPasswordEncoder,
                          Utils utils) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.bugRepo = bugRepo;


        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.utils = utils;
    }


    @Override
    public boolean isUserExistsByEmail(String email) {
        return userRepo.existsByEmail(email);
    }

    private UserEntity getUserEntity(String id) {
        return userRepo.findByPublicId(id)
                .orElseThrow(() -> new UserServiceException(NO_RECORD_FOUND, id));
    }

    @Override
    public UserDto getUserById(String id) {
        UserEntity userEntity = getUserEntity(id);
        return modelMapper.map(userEntity, UserDto.class);
    }

    @Transactional
    @Override
    public UserDto getUserByEmail(String email) {
        UserEntity userEntity = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserServiceException(NO_RECORD_FOUND, email));

        return modelMapper.map(userEntity, UserDto.class);
    }


    @Override
    public UserDto createAdminUser(String email, String password, Set<RoleEntity> roles) {
        if (userRepo.existsByEmail(email)) throw new UserServiceException(RECORD_ALREADY_EXISTS, email);
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(email);
        userEntity.setUsername("Admiral");
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
        UserEntity userEntity = getUserEntity(id);
        userEntity.setUsername(userDto.getUsername());

        return modelMapper.map(userRepo.save(userEntity), UserDto.class);
    }

    @Transactional
    @Override
    public void deleteUser(String id) {
        userRepo.delete(getUserEntity(id));
    }

    @Override
    public Set<BugDto> getReportedBugs(String id, int page, int limit) {
        if (page-- < 0 || limit < 1) throw new IllegalArgumentException();

        List<BugEntity> bugs =
                bugRepo.findAllByReporter(getUserEntity(id), PageRequest.of(page, limit));
        return modelMapper.map(bugs, new TypeToken<Set<BugDto>>() {
        }.getType());
    }

    @Override
    public Set<BugDto> getGetWorkingOnBugs(String id, int page, int limit) {
        if (page < 1 || limit < 1) throw new IllegalArgumentException();

        Set<BugDto> workingOnBugs = getUserById(id).getWorkingOnBugs();

        PageImpl<BugDto> pagedBugs =
                new PageImpl<>(
                        workingOnBugs.stream().toList(),
                        Pageable.ofSize(page), limit);

        return new LinkedHashSet<>(pagedBugs.getContent());
    }

    @Override
    public Set<ProjectDto> getSubscribedProjects(String id, int page, int limit) {
        if (page < 1 || limit < 1) throw new IllegalArgumentException();

        Set<ProjectDto> subscribedProjects = getUserById(id).getSubscribedToProjects();

        PageImpl<ProjectDto> pagedProjects =
                new PageImpl<>(
                        subscribedProjects.stream().toList(),
                        Pageable.ofSize(page), limit);

        return new LinkedHashSet<>(pagedProjects.getContent());
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(NO_RECORD_FOUND + " with : " +email));

        return new UserPrincipal(userEntity);
    }
}
