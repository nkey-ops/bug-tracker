package com.bluesky.bugtraker.setup;

import com.bluesky.bugtraker.io.entity.RoleEntity;
import com.bluesky.bugtraker.io.repository.RoleRepository;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.shared.authorizationenum.Role;
import com.bluesky.bugtraker.shared.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.persistence.Transient;

import static com.bluesky.bugtraker.shared.authorizationenum.Role.*;

@Component
public class InitialUserSetup {
    @Value("${super-admin-user.username}")
    private String superAdminUsername;
    @Value("${super-admin-user.email}")
    private String superAdminEmail;
    @Value("${super-admin-user.password}")
    private String superAdminPassword;

    @Value("${is-super-admin-email-verified}")
    private boolean isSuperAdminVerified;	
    		
    private final RoleRepository roleRepo;
    private final UserService userService;

    public InitialUserSetup(RoleRepository roleRepo, UserService userService) {
        this.roleRepo = roleRepo;
        this.userService = userService;
    }

    @EventListener
    @Transient
    public void onApplicationEvent(ApplicationReadyEvent readyEvent) {
        createRole(ROLE_USER);
        createRole(ROLE_ADMIN);
        createRole(ROLE_SUPER_ADMIN);

        UserDTO userDTO = new UserDTO();
        userDTO.setRole(ROLE_SUPER_ADMIN);
        userDTO.setUsername(superAdminUsername);
        userDTO.setPassword(superAdminPassword);
        userDTO.setEmail(superAdminEmail);
        userDTO.setEmailVerificationStatus(isSuperAdminVerified);

        if(!userService.existsUserByEmail(superAdminEmail))
                userService.createUserWithRole(userDTO);
    }

    @Transient
    private RoleEntity createRole(Role role) {
        if (roleRepo.findByRole(role).isPresent())
            return roleRepo.findByRole(role).get();
        else {
            return roleRepo.save(new RoleEntity(role));
        }
    }

}
