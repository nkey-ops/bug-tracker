package com.bluesky.bugtraker.service.impl;

import com.bluesky.bugtraker.exceptions.serviceexception.ProjectServiceException;
import com.bluesky.bugtraker.exceptions.serviceexception.RoleServiceException;
import com.bluesky.bugtraker.exceptions.serviceexception.TicketServiceException;
import com.bluesky.bugtraker.exceptions.serviceexception.UserServiceException;
import com.bluesky.bugtraker.io.entity.RoleEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.repository.ProjectRepository;
import com.bluesky.bugtraker.io.repository.RoleRepository;
import com.bluesky.bugtraker.io.repository.TicketRepository;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.security.UserPrincipal;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.service.utils.ServiceUtils;
import com.bluesky.bugtraker.service.utils.Utils;
import com.bluesky.bugtraker.shared.authorizationenum.Role;
import com.bluesky.bugtraker.shared.dto.ProjectsInfoDTO;
import com.bluesky.bugtraker.shared.dto.UserDTO;
import com.bluesky.bugtraker.shared.dto.UserInfoDTO;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.Period;
import java.util.*;

import static com.bluesky.bugtraker.exceptions.ErrorType.NO_RECORD_FOUND;
import static com.bluesky.bugtraker.exceptions.ErrorType.RECORD_ALREADY_EXISTS;
import static com.bluesky.bugtraker.service.specifications.Specs.*;

@Service
public class UserServiceImp implements UserService {
    @Value("${user-avatar-url}")
    private String avatarURL;

    private final ServiceUtils serviceUtils;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final TicketRepository ticketRepo;
    private final ProjectRepository projectRepo;


    private final Utils utils;
    private final ModelMapper modelMapper;

    @Autowired
    public UserServiceImp(ServiceUtils serviceUtils,
                          UserRepository userRepo,
                          RoleRepository roleRepo,
                          TicketRepository ticketRepo,
                          ProjectRepository projectRepo,
                          Utils utils,
                          ModelMapper modelMapper) {
        this.serviceUtils = serviceUtils;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.ticketRepo = ticketRepo;
        this.projectRepo = projectRepo;

        this.utils = utils;
        this.modelMapper = modelMapper;
    }


    @Override
    public boolean isUserExistsByEmail(String email) {
        return userRepo.existsByEmail(email);
    }


    @Override
    @NotNull
    public boolean existsUserByEmail(@NotNull String email) {
        return userRepo.existsByEmail(email);
    }

    @Override
    @NotNull
    public UserDTO getUserById(@NotNull String id) {
        UserEntity userEntity = serviceUtils.getUserEntity(id);
        UserDTO userDTO = modelMapper.map(userEntity, UserDTO.class);
        userDTO.setRole(userEntity.getRoleEntity().getRole());

        return userDTO;
    }

    @Override
    @NotNull
    public UserDTO getUserByEmail(@NotNull String email) {
        UserEntity userEntity = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserServiceException(NO_RECORD_FOUND, email));

        UserDTO userDTO = modelMapper.map(userEntity, UserDTO.class);
        userDTO.setRole(userEntity.getRoleEntity().getRole());

        return userDTO;
    }

    @Override
    @NotNull
    public DataTablesOutput<UserDTO> getUsers(DataTablesInput input) {

        DataTablesOutput<UserEntity> userEntities = userRepo.findAll(input);

        return utils.map(userEntities, new TypeToken<>() {
        });
    }


    @Transactional
    public UserDTO createUserWithRole(@NotNull UserDTO userDto) {
        if (userRepo.existsByEmail(userDto.getEmail()))
            throw new UserServiceException(RECORD_ALREADY_EXISTS, userDto.getEmail());

        UserEntity userEntity = modelMapper.map(userDto, UserEntity.class);
        userEntity.setPublicId(utils.generateUserId());
        userEntity.setEncryptedPassword(utils.encode(userDto.getPassword()));
        userEntity.setAvatarURL(avatarURL);
        userEntity.setEmailVerificationToken("mock");
        userEntity.setEmailVerificationStatus(true);   // TODO after testing change to false;

        RoleEntity roleEntity = roleRepo.findByRole(userDto.getRole())
                .orElseThrow(() ->
                        new RoleServiceException(NO_RECORD_FOUND, userDto.getRole().toString()));
        userEntity.setRoleEntity(roleEntity);

        UserEntity savedEntity = userRepo.save(userEntity);

        return modelMapper.map(savedEntity, UserDTO.class);
    }

    @Override
    public UserDTO createUser(UserDTO userDto) {
        userDto.setRole(Role.ROLE_USER);
        return createUserWithRole(userDto);
    }

    @Override
    public void updateUser(String userId, UserDTO userDto) {
        UserEntity userEntity = serviceUtils.getUserEntity(userId);

        if (userDto.getRole() != null) {
            RoleEntity roleEntity =
                    serviceUtils.getRoleEntityToBeSet(userDto.getRole(), userEntity.getRoleEntity().getRole());
            userEntity.setRoleEntity(roleEntity);
        }

        String avatarURL = userDto.getAvatarURL();
        if (avatarURL == null || avatarURL.isBlank())
            userDto.setAvatarURL(this.avatarURL);

        modelMapper.map(userDto, userEntity);
        userRepo.save(userEntity);
    }

    @Override
    public void deleteUser(@NotNull String userId) {
        UserEntity userEntity = serviceUtils.getUserEntity(userId);
        userRepo.delete(userEntity);
    }

    @Override
    @NotNull
    public ProjectsInfoDTO getProjectsInfo(String userId) {
        UserEntity userEntity = serviceUtils.getUserEntity(userId);

        Date dayAgo =  Date.from(Instant.now().minus(Period.ofDays(1)));
        ProjectsInfoDTO projectsInfoDTO = new ProjectsInfoDTO();

        projectsInfoDTO.setTicketsReported(
                ticketRepo.countAllByProjectCreator(userEntity));
        projectsInfoDTO.setTicketsReportedADayAgo(
                ticketRepo.countAllByProjectCreatorAndLastUpdateTimeAfter(userEntity, dayAgo));

        projectsInfoDTO.setCriticalTickets(
                ticketRepo.countAllByProjectCreatorAndSeverity(userEntity, Severity.CRITICAL));
        projectsInfoDTO.setCriticalTicketsADayAgo(
                ticketRepo.countAllByProjectCreatorAndSeverityAndLastUpdateTimeAfter(userEntity, Severity.CRITICAL, dayAgo));

        projectsInfoDTO.setCompletedTickets(
                ticketRepo.countAllByProjectCreatorAndStatus(userEntity, Status.COMPLETED));
        projectsInfoDTO.setCompletedTicketsADayAgo(
                ticketRepo.countAllByProjectCreatorAndStatusAndLastUpdateTime(userEntity, Status.COMPLETED, dayAgo));

        projectsInfoDTO.setTicketsInProgress(
                ticketRepo.countAllByProjectCreatorAndStatus(userEntity, Status.IN_PROGRESS));
        projectsInfoDTO.setTicketInProgressADayAgo(
                ticketRepo.countAllByProjectCreatorAndStatusAndLastUpdateTime(userEntity, Status.IN_PROGRESS, dayAgo));


        Map<Status, List<Long>> ticketsPerWeek = new EnumMap<>(Status.class);
        projectsInfoDTO.setTicketsPerWeek(ticketsPerWeek);

        for (int i = 0; i < 5; i++) {
            long countToFix = ticketRepo.count(ticketByProjectCreator(userEntity).and(statusIs(Status.TO_FIX).and(lastUpdatedWeeksAgo(i))));
            long countInProgress = ticketRepo.count(ticketByProjectCreator(userEntity).and(statusIs(Status.IN_PROGRESS).and(lastUpdatedWeeksAgo(i))));
            long countCompleted = ticketRepo.count(ticketByProjectCreator(userEntity).and(statusIs(Status.COMPLETED).and(lastUpdatedWeeksAgo(i))));

            ticketsPerWeek.putIfAbsent(Status.TO_FIX, new ArrayList<>());
            ticketsPerWeek.putIfAbsent(Status.IN_PROGRESS, new ArrayList<>());
            ticketsPerWeek.putIfAbsent(Status.COMPLETED, new ArrayList<>());

            ticketsPerWeek.get(Status.TO_FIX).add(countToFix);
            ticketsPerWeek.get(Status.IN_PROGRESS).add(countInProgress);
            ticketsPerWeek.get(Status.COMPLETED).add(countCompleted);
        }

        Map<Status, Long> tickets = new EnumMap<>(Status.class);
        projectsInfoDTO.setTickets(tickets);

        tickets.put(Status.TO_FIX, ticketRepo.countAllByProjectCreatorAndStatus(userEntity, Status.TO_FIX));
        tickets.put(Status.IN_PROGRESS, ticketRepo.countAllByProjectCreatorAndStatus(userEntity, Status.IN_PROGRESS));
        tickets.put(Status.COMPLETED, ticketRepo.countAllByProjectCreatorAndStatus(userEntity, Status.COMPLETED));

        return projectsInfoDTO;
    }

    @Override
    @NotNull
    public UserInfoDTO getUserInfo(String userId) {
        UserEntity userEntity = serviceUtils.getUserEntity(userId);

        long ticketsReported = ticketRepo.count(byReporter(userEntity));
        long ticketsReportedADayAgo = ticketRepo.count(byReporter(userEntity).and(reportedADayAgo()));
        long ticketsReportedAMonthAgo = ticketRepo.count(byReporter(userEntity).and(reportedAMonthAgo()));

        long ticketsCompleted = ticketRepo.count(bySubscriber(userEntity).and(statusIsCompleted()));
        long ticketsCompletedADayAgo = ticketRepo.count(bySubscriber(userEntity).and(statusIsCompleted()).and(reportedADayAgo()));
        long ticketsCompletedAMonthAgo = ticketRepo.count(bySubscriber(userEntity).and(statusIsCompleted()).and(reportedAMonthAgo()));

        long ticketsSubscribedTo = ticketRepo.count(bySubscriber(userEntity));
        long ticketsSubscribedToADayAgo = ticketRepo.count(bySubscriber(userEntity).and(reportedADayAgo()));
        long ticketsSubscribedToAMonthAgo = ticketRepo.count(bySubscriber(userEntity).and(reportedAMonthAgo()));

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

    public boolean isSubscribedToProject(@NotNull String userId, @NotNull String projectId) {
        if (!projectRepo.existsByPublicId(projectId))
            throw new ProjectServiceException(NO_RECORD_FOUND, projectId);
        
        UserEntity userEntity = serviceUtils.getUserEntity(userId);

        return projectRepo.existsByPublicIdAndSubscribersIn(projectId, Set.of(userEntity));
    }

    public boolean isSubscribedToTicket(@NotNull String userId, @NotNull String ticketId) {
        if (!ticketRepo.existsByPublicId(ticketId))
            throw new TicketServiceException(NO_RECORD_FOUND, ticketId);
        
        UserEntity userEntity = serviceUtils.getUserEntity(userId);

        return ticketRepo.existsByPublicIdAndSubscribersIn(ticketId, Set.of(userEntity));
    }

    public boolean isSuperAdmin(@NotNull String userId) {
        return serviceUtils.getUserEntity(userId).getRoleEntity().getRole() == Role.ROLE_SUPER_ADMIN;
    }


    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(NO_RECORD_FOUND + " with : " + email));

        UserDTO userDto = modelMapper.map(userEntity, UserDTO.class);

        return new UserPrincipal(userDto);
    }
}
