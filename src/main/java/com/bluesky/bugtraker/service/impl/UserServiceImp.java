package com.bluesky.bugtraker.service.impl;

import static com.bluesky.bugtraker.exceptions.ErrorType.INTERNAL_SERVER_ERROR;
import static com.bluesky.bugtraker.exceptions.ErrorType.NO_RECORD_FOUND;
import static com.bluesky.bugtraker.exceptions.ErrorType.RECORD_ALREADY_EXISTS;
import static com.bluesky.bugtraker.service.specifications.Specs.byReporter;
import static com.bluesky.bugtraker.service.specifications.Specs.bySubscriber;
import static com.bluesky.bugtraker.service.specifications.Specs.reportedADayAgo;
import static com.bluesky.bugtraker.service.specifications.Specs.reportedAMonthAgo;
import static com.bluesky.bugtraker.service.specifications.Specs.statusIs;

import java.time.Instant;
import java.time.Period;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

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

import com.bluesky.bugtraker.exceptions.ErrorType;
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
import com.bluesky.bugtraker.service.utils.DataExtractionUtils;
import com.bluesky.bugtraker.service.utils.UserServiceUtils;
import com.bluesky.bugtraker.service.utils.Utils;
import com.bluesky.bugtraker.shared.authorizationenum.Role;
import com.bluesky.bugtraker.shared.dto.ProjectsInfoDTO;
import com.bluesky.bugtraker.shared.dto.UserDTO;
import com.bluesky.bugtraker.shared.dto.UserInfoDTO;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;

@Service
public class UserServiceImp implements UserService {
    @Value("${user-avatar-url}")
    private String avatarURL;

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final TicketRepository ticketRepo;
    private final ProjectRepository projectRepo;


    private final DataExtractionUtils dataExtractionUtils;
    private final UserServiceUtils userServiceUtils;
    private final Utils utils;
    private final ModelMapper modelMapper;

	private final EmailServiceImpl emailService;

    public UserServiceImp(
    					DataExtractionUtils dataExtractionUtils, 
    					UserRepository userRepo, 
    					RoleRepository roleRepo, 
    					TicketRepository ticketRepo, 
    					ProjectRepository projectRepo, 
    					UserServiceUtils userServiceUtils, 
    					EmailServiceImpl emailServiceImpl,
    					Utils utils, ModelMapper modelMapper) {

        this.dataExtractionUtils = dataExtractionUtils;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.ticketRepo = ticketRepo;
        this.projectRepo = projectRepo;
        this.userServiceUtils = userServiceUtils;
		this.emailService = emailServiceImpl;

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
        UserEntity userEntity = dataExtractionUtils.getUserEntity(id);
        UserDTO userDTO = modelMapper.map(userEntity, UserDTO.class);
        userDTO.setRole(userEntity.getRoleEntity().getRole());

        return userDTO;
    }

    @Override
    @NotNull
    public UserDTO getUserByEmail(@NotNull String email) {
        UserEntity userEntity = userRepo.findByEmail(email).orElseThrow(() -> new UserServiceException(NO_RECORD_FOUND, email));

        UserDTO userDTO = modelMapper.map(userEntity, UserDTO.class);
        userDTO.setRole(userEntity.getRoleEntity().getRole());

        return userDTO;
    }

    @Override
    @NotNull
    public DataTablesOutput<UserDTO> getUsers(@NotNull DataTablesInput input) {

        DataTablesOutput<UserEntity> userEntities = userRepo.findAll(input);

        return utils.map(userEntities, new TypeToken<>() {
        });
    }


    @Transactional
    @NotNull
    public UserDTO createUserWithRole(@NotNull UserDTO userDto) {
        if (userRepo.existsByEmail(userDto.getEmail()))
            throw new UserServiceException(RECORD_ALREADY_EXISTS, userDto.getEmail());

        UserEntity userEntity = modelMapper.map(userDto, UserEntity.class);
        userEntity.setPublicId(utils.generateUserId());
        userEntity.setEncryptedPassword(utils.encode(userDto.getPassword()));
        userEntity.setAvatarURL(avatarURL);

        RoleEntity roleEntity = roleRepo.findByRole(
        		userDto.getRole())
        		.orElseThrow(() -> 
        			new RoleServiceException(NO_RECORD_FOUND, userDto.getRole().toString()));

        userEntity.setRoleEntity(roleEntity);

        if(!userDto.isEmailVerificationStatus()) {
        	userEntity.setEmailVerificationToken(
        		utils.getEmailVerificationToken(userEntity.getPublicId()));

        	emailService.verifyEmail(
        		userEntity.getEmail(), userEntity.getEmailVerificationToken());
        } 
        

        UserEntity savedEntity = userRepo.save(userEntity);

       
        return modelMapper.map(savedEntity, UserDTO.class);
    }

    @Override
    @NotNull
    public UserDTO createUser(@NotNull UserDTO userDto) {
        userDto.setRole(Role.ROLE_USER);
        return createUserWithRole(userDto);
    }

    @Override
    public void updateUser(@NotNull String userId, @NotNull UserDTO userDto) {
        UserEntity userEntity = dataExtractionUtils.getUserEntity(userId);

        if (userDto.getRole() != null) {
            RoleEntity roleEntity = dataExtractionUtils.getRoleEntityToBeSet(userDto.getRole(), userEntity.getRoleEntity().getRole());
            userEntity.setRoleEntity(roleEntity);
        }

        String avatarURL = userDto.getAvatarURL();
        if (avatarURL == null || avatarURL.isBlank()) userDto.setAvatarURL(this.avatarURL);

        modelMapper.map(userDto, userEntity);
        userRepo.save(userEntity);
    }

    @Override
    public void deleteUser(@NotNull String userId) {
        UserEntity userEntity = dataExtractionUtils.getUserEntity(userId);

        if(userEntity.getRoleEntity().getRole() == Role.ROLE_SUPER_ADMIN
                && userRepo.countByRoleEntityRole(Role.ROLE_SUPER_ADMIN) <= 1)
            
            throw new UserServiceException(INTERNAL_SERVER_ERROR,
                    "At least one user with role " + Role.ROLE_SUPER_ADMIN + " should exists");

        userRepo.delete(userEntity);
    }

    @Override
    @NotNull
    public ProjectsInfoDTO getProjectsInfo(String userId) {
        UserEntity userEntity = dataExtractionUtils.getUserEntity(userId);

        Date dayAgo = Date.from(Instant.now().minus(Period.ofDays(1)));
        ProjectsInfoDTO projectsInfoDTO = new ProjectsInfoDTO();

        long ticketsReported = ticketRepo.countAllByProjectCreator(userEntity);
        long ticketsReportedADayAgo = ticketRepo.countAllByProjectCreatorAndLastUpdateTimeAfter(userEntity, dayAgo);
        
        long criticalTickets = ticketRepo.countAllByProjectCreatorAndSeverity(userEntity, Severity.CRITICAL);
        long criticalTicketsADayAgo = ticketRepo.countAllByProjectCreatorAndSeverityAndLastUpdateTimeAfter(userEntity, Severity.CRITICAL, dayAgo);
        
        long completedTickets = ticketRepo.countAllByProjectCreatorAndStatus(userEntity, Status.COMPLETED);
        long completedTicketsADayAgo = ticketRepo.countAllByProjectCreatorAndStatusAndLastUpdateTime(userEntity, Status.COMPLETED, dayAgo);
       
        long ticketsInProgress = ticketRepo.countAllByProjectCreatorAndStatus(userEntity, Status.IN_PROGRESS);
        long ticketInProgressADayAgo = ticketRepo.countAllByProjectCreatorAndStatusAndLastUpdateTime(userEntity, Status.IN_PROGRESS, dayAgo);
       
        projectsInfoDTO.setTicketsReported(ticketsReported);
        projectsInfoDTO.setTicketsReportedADayAgo(ticketsReportedADayAgo);

        projectsInfoDTO.setCriticalTickets(criticalTickets);
        projectsInfoDTO.setCriticalTicketsADayAgo(criticalTicketsADayAgo);

        projectsInfoDTO.setCompletedTickets(completedTickets);
        projectsInfoDTO.setCompletedTicketsADayAgo(completedTicketsADayAgo);

        projectsInfoDTO.setTicketsInProgress(ticketsInProgress);
        projectsInfoDTO.setTicketInProgressADayAgo(ticketInProgressADayAgo);


        Map<Status, List<Long>> ticketsStatusPerWeek = userServiceUtils.getTicketsStatusPerWeek(userEntity);
        projectsInfoDTO.setTicketsStatusPerWeek(ticketsStatusPerWeek);

        Map<Status, Long> ticketsStatusOverall = userServiceUtils.getTicketsStatusOverall(userEntity);
        projectsInfoDTO.setTicketsStatusOverall(ticketsStatusOverall);

        return projectsInfoDTO;
    }

    @Override
    @NotNull
    public UserInfoDTO getUserInfo(String userId) {
        UserEntity userEntity = dataExtractionUtils.getUserEntity(userId);

        long ticketsReported = ticketRepo.count(byReporter(userEntity));
        long ticketsReportedADayAgo = ticketRepo.count(byReporter(userEntity).and(reportedADayAgo()));
        long ticketsReportedAMonthAgo = ticketRepo.count(byReporter(userEntity).and(reportedAMonthAgo()));

        long ticketsCompleted = ticketRepo.count(bySubscriber(userEntity).and(statusIs(Status.COMPLETED)));
        long ticketsCompletedADayAgo = ticketRepo.count(bySubscriber(userEntity).and(statusIs(Status.COMPLETED)).and(reportedADayAgo()));
        long ticketsCompletedAMonthAgo = ticketRepo.count(bySubscriber(userEntity).and(statusIs(Status.COMPLETED)).and(reportedAMonthAgo()));

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


        Map<Severity, List<Long>> ticketsSeverityPerWeek = userServiceUtils.getTicketsSeverityPerWeek(userEntity);
        userInfoDTO.setTicketsSeverityPerWeek(ticketsSeverityPerWeek);

        Map<Severity, Long> tickets = userServiceUtils.getTicketsSeverityOverall(userEntity);
        userInfoDTO.setTicketsSeverityOverall(tickets);

        return userInfoDTO;
    }

    public boolean isSubscribedToProject(@NotNull String userId, @NotNull String projectId) {
        if (!projectRepo.existsByPublicId(projectId)) throw new ProjectServiceException(NO_RECORD_FOUND, projectId);

        UserEntity userEntity = dataExtractionUtils.getUserEntity(userId);

        return projectRepo.existsByPublicIdAndSubscribersIn(projectId, Set.of(userEntity));
    }

    public boolean isSubscribedToTicket(@NotNull String userId, @NotNull String ticketId) {
        if (!ticketRepo.existsByPublicId(ticketId)) throw new TicketServiceException(NO_RECORD_FOUND, ticketId);

        UserEntity userEntity = dataExtractionUtils.getUserEntity(userId);

        return ticketRepo.existsByPublicIdAndSubscribersIn(ticketId, Set.of(userEntity));
    }

    public boolean isSuperAdmin(@NotNull String userId) {
        return dataExtractionUtils.getUserEntity(userId).getRoleEntity().getRole() == Role.ROLE_SUPER_ADMIN;
    }


    @Override
    @Transactional
    public UserDetails loadUserByUsername(@NotNull String email) throws UsernameNotFoundException {
        UserEntity userEntity = userRepo.findByEmail(email)
                .orElseThrow(() -> 
                        new UsernameNotFoundException(NO_RECORD_FOUND + " with : " + email));

        UserDTO userDto = modelMapper.map(userEntity, UserDTO.class);



        return new UserPrincipal(userDto);
    }


	@Override
	public void verifyEmailToken(@NotNull String token) {
		UserEntity userEntity = userRepo.findByEmailVerificationToken(token)
			.orElseThrow(() -> 
					new UserServiceException(NO_RECORD_FOUND, 
							"Cannot find the verification token"));
		
		if(utils.hasEmailTokenExpired(token))
			throw new UserServiceException(
					ErrorType.EMAIL_VERIFICATION_TOKEN_IS_EXPIRED);

		userEntity.setEmailVerificationToken(null);;
		userEntity.setEmailVerificationStatus(true);
		
		userRepo.save(userEntity);
	}
}
