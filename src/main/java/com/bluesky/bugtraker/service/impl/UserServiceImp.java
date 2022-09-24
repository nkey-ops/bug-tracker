package com.bluesky.bugtraker.service.impl;

import com.bluesky.bugtraker.exceptions.serviceexception.RoleServiceException;
import com.bluesky.bugtraker.exceptions.serviceexception.UserServiceException;
import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.TicketEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.entity.authorization.RoleEntity;
import com.bluesky.bugtraker.io.repository.ProjectRepository;
import com.bluesky.bugtraker.io.repository.RoleRepository;
import com.bluesky.bugtraker.io.repository.TicketRepository;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.security.UserPrincipal;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.service.Utils;
import com.bluesky.bugtraker.shared.authorizationenum.Role;
import com.bluesky.bugtraker.shared.dto.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static com.bluesky.bugtraker.exceptions.ErrorMessages.NO_RECORD_FOUND;
import static com.bluesky.bugtraker.exceptions.ErrorMessages.RECORD_ALREADY_EXISTS;
import static com.bluesky.bugtraker.service.specifications.Specs.*;
import static org.springframework.data.jpa.domain.Specification.where;

@Service
public class UserServiceImp implements UserService {
    //TODO make a constant values for user id length;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final TicketRepository ticketRepo;
    private final ProjectRepository projectRepo;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Utils utils;
    private final ModelMapper modelMapper;

    @Autowired
    public UserServiceImp(UserRepository userRepo, RoleRepository roleRepo,
                          TicketRepository ticketRepo,
                          ProjectRepository projectRepo, BCryptPasswordEncoder bCryptPasswordEncoder,
                          Utils utils, ModelMapper modelMapper) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.ticketRepo = ticketRepo;
        this.projectRepo = projectRepo;


        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.utils = utils;
        this.modelMapper = modelMapper;
    }


    @Override
    public boolean isUserExistsByEmail(String email) {
        return userRepo.existsByEmail(email);
    }

    private UserEntity getUserEntity(String id) {
        if (id == null) throw new NullPointerException("Id cannot be null");

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
        if (email == null)
            throw new NullPointerException("Email cannot be null");

        UserEntity userEntity = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserServiceException(NO_RECORD_FOUND, email));

        return modelMapper.map(userEntity, UserDto.class);
    }


    public UserDto createUserWithRoles(UserDto userDto, Set<Role> roles) {
        if (userRepo.existsByEmail(userDto.getEmail()))
            throw new UserServiceException(RECORD_ALREADY_EXISTS, userDto.getEmail());

        UserEntity userEntity = modelMapper.map(userDto, UserEntity.class);

        userEntity.setPublicId(utils.generateUserId(30));
        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));
        userEntity.setEmailVerificationToken("mock");
        //        TODO after testing change to false;
        userEntity.setEmailVerificationStatus(true);

        Set<RoleEntity> roleEntities = roleRepo.findAllByRoleIn(roles)
                .orElseThrow(() ->
                        new RoleServiceException(NO_RECORD_FOUND, roles.toString()));
        userEntity.setRoles(roleEntities);

        UserEntity savedEntity = userRepo.save(userEntity);

        return modelMapper.map(savedEntity, UserDto.class);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        return createUserWithRoles(userDto, Set.of(Role.ROLE_USER));
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
        if (id == null)
            throw new NullPointerException("Id cannot be null");

        if (!userRepo.existsByPublicId(id)) {
            throw new UserServiceException(NO_RECORD_FOUND, id);
        }

        userRepo.delete(getUserEntity(id));
    }

    @Override
    public Page<TicketDto> getReportedTickets(String userId, int page, int limit) {
        if (page < 1 || limit < 1) throw new IllegalArgumentException();

        Page<TicketEntity> bugs =
                ticketRepo.findAllByReporter(
                        getUserEntity(userId), PageRequest.of(page - 1, limit));
        return modelMapper.map(bugs, new TypeToken<Page<TicketDto>>() {
        }.getType());
    }

    @Override
    public Page<TicketDto> getWorkingOnTickets(String userId, int page, int limit) {
        if (page < 1 || limit < 1) throw new IllegalArgumentException();

        Page<TicketEntity> ticketFixers = ticketRepo.findAllByAssignedDevsIn(
                Set.of(getUserEntity(userId)), PageRequest.of(page - 1, limit));
        return modelMapper.map(ticketFixers, new TypeToken<Page<TicketDto>>() {
        }.getType());
    }


    @Override
    public Page<ProjectDto> getSubscribedOnProjects(String userId, int page, int limit) {
        if (page < 1 || limit < 1) throw new IllegalArgumentException();

        Page<ProjectEntity> projectEntities = projectRepo.findAllBySubscribersIn(
                Set.of(getUserEntity(userId)), PageRequest.of(page - 1, limit));
        return modelMapper.map(projectEntities, new TypeToken<Page<ProjectDto>>() {
        }.getType());

    }

    @Override
    public TicketsInfoDTO getTicketsInfo(String userId) {
        TicketsInfoDTO ticketsInfoDTO = new TicketsInfoDTO();

        Set<ProjectEntity> allCreatedProjects =
                projectRepo.findAllByCreator(getUserEntity(userId));

        long ticketsReported = 0;
        long ticketsReportedADayAgo = 0;

        long criticalTickets = 0;
        long criticalTicketsADayAgo = 0;

        long completedTickets = 0;
        long completedTicketsADayAgo = 0;

        long inProgressTickets = 0;
        long inProgressTicketsADayAgo = 0;

        for (ProjectEntity projectEntity : allCreatedProjects) {
            Long projectId = projectEntity.getId();

            Specification<TicketEntity> byProjectIdAndADayAgo =
                    where(byProjectId(projectId).and(reportedADayAgo()));

            ticketsReported += ticketRepo.count(byProjectId(projectId));
            ticketsReportedADayAgo += ticketRepo.count(byProjectIdAndADayAgo);

            criticalTickets += ticketRepo.count(byProjectId(projectId).and(severityIsCritical()));
            criticalTicketsADayAgo += ticketRepo.count(byProjectIdAndADayAgo.and(severityIsCritical()));

            completedTickets += ticketRepo.count(byProjectId(projectId).and(statusIsCompleted()));
            completedTicketsADayAgo += ticketRepo.count(byProjectIdAndADayAgo.and(statusIsCompleted()));

            inProgressTickets += ticketRepo.count(byProjectId(projectId).and(statusIsInProgress()));
            inProgressTicketsADayAgo += ticketRepo.count(byProjectIdAndADayAgo.and(statusIsInProgress()));
        }


        ticketsInfoDTO.setTicketsReported(ticketsReported);
        ticketsInfoDTO.setTicketsReportedADayAgo(ticketsReportedADayAgo);

        ticketsInfoDTO.setCriticalTickets(criticalTickets);
        ticketsInfoDTO.setCriticalTicketsADayAgo(criticalTicketsADayAgo);

        ticketsInfoDTO.setCompletedTickets(completedTickets);
        ticketsInfoDTO.setCompletedTicketsADayAgo(completedTicketsADayAgo);

        ticketsInfoDTO.setTicketsInProgress(inProgressTickets);
        ticketsInfoDTO.setTicketInProgressADayAgo(inProgressTicketsADayAgo);


        return ticketsInfoDTO;
    }

    @Override
    public UserInfoDTO getUserInfo(String userId) {
        UserEntity userEntity = getUserEntity(userId);

        long ticketsReported = ticketRepo.count(byReporter(userEntity));
        long ticketsReportedADayAgo = ticketRepo.count(byReporter(userEntity).and(reportedADayAgo()));
        long ticketsReportedAMonthAgo = ticketRepo.count(byReporter(userEntity).and(reportedAMonthAgo()));

        long ticketsCompleted = ticketRepo.count(byAssigndedDev(userEntity).and(statusIsCompleted()));
        long ticketsCompletedADayAgo = ticketRepo.count(byAssigndedDev(userEntity).and(statusIsCompleted()).and(reportedADayAgo()));
        long ticketsCompletedAMonthAgo = ticketRepo.count(byAssigndedDev(userEntity).and(statusIsCompleted()).and(reportedAMonthAgo()));
        
        long ticketsSubscribedTo = ticketRepo.count(byAssigndedDev(userEntity));
        long ticketsSubscribedToADayAgo = ticketRepo.count(byAssigndedDev(userEntity).and(reportedADayAgo()));
        long ticketsSubscribedToAMonthAgo = ticketRepo.count(byAssigndedDev(userEntity).and(reportedAMonthAgo()));
        
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setTicketsReported(ticketsReported);
        userInfoDTO.setTicketsReportedADayAgo(ticketsReportedADayAgo);
        userInfoDTO.setTicketsReportedAMonthAgo(ticketsReportedAMonthAgo);
        
        userInfoDTO.setTicketsCompleted(ticketsCompleted);
        userInfoDTO.setTicketsCompletedADayAgo(ticketsCompletedADayAgo);
        userInfoDTO.setTicketsCompletedAMonthAgo(ticketsCompletedAMonthAgo);
        
        userInfoDTO.setTicketsSubscribedTo(ticketsSubscribedTo);
        userInfoDTO.setTicketsSubscribedToADayAgo(ticketsSubscribedToADayAgo);
        userInfoDTO.setTicketsSubscribedToAMonthAgo(ticketsSubscribedToAMonthAgo);

        return userInfoDTO;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(NO_RECORD_FOUND + " with : " + email));

        UserDto userDto = modelMapper.map(userEntity, UserDto.class);

        return new UserPrincipal(userDto);
    }
}
