package com.bluesky.bugtraker.setup;

import com.bluesky.bugtraker.io.entity.authorization.AuthorityEntity;
import com.bluesky.bugtraker.io.entity.authorization.RoleEntity;
import com.bluesky.bugtraker.io.repository.AuthorityRepository;
import com.bluesky.bugtraker.io.repository.RoleRepository;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.shared.authorizationenum.Authority;
import com.bluesky.bugtraker.shared.authorizationenum.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.bluesky.bugtraker.shared.authorizationenum.Authority.*;
import static com.bluesky.bugtraker.shared.authorizationenum.Role.*;

@Component
public class InitialUserSetup {
    @Value( "${superAdminUser.email}" )
    private String superAdminUserEmail;

    @Value( "${superAdminUser.password}" )
    private String superAdminUserPassword;


    private final AuthorityRepository authorityRepo;
    private RoleRepository roleRepo;

    private UserService userService;

    @Autowired
    public InitialUserSetup(AuthorityRepository authorityRepo, RoleRepository roleRepo, UserService userService) {
        this.authorityRepo = authorityRepo;
        this.roleRepo = roleRepo;
        this.userService = userService;
    }


    @EventListener
    @Transient
    public void onApplicationEvent(ApplicationReadyEvent readyEvent) {
        AuthorityEntity readAuthority = createAuthority(READ_AUTHORITY);
        AuthorityEntity writeAuthority = createAuthority(WRITE_AUTHORITY);
        AuthorityEntity deleteAuthority = createAuthority(DELETE_AUTHORITY);

        AuthorityEntity creatAdminAuthority = createAuthority(CREATE_ADMIN_AUTHORITY);
        AuthorityEntity deleteAdminAuthority = createAuthority(DELETE_ADMIN_AUTHORITY);

        List<AuthorityEntity> authorities = new ArrayList<>(Arrays.asList(readAuthority, writeAuthority));
        createRole(ROLE_USER, authorities);

        authorities.add(deleteAuthority);
        RoleEntity adminRole = createRole(ROLE_ADMIN,authorities);

        authorities.addAll(Arrays.asList(creatAdminAuthority, deleteAdminAuthority));
        RoleEntity superAdminRole = createRole(ROLE_SUPER_ADMIN, authorities);

//        userService.createAdminUser(superAdminUserEmail, superAdminUserPassword, Set.of(superAdminRole));
    }

    //    TODO try to use streams


    @Transient
    private AuthorityEntity createAuthority(Authority authority) {
        if (authorityRepo.findByAuthority(authority).isPresent()) {
            return authorityRepo.findByAuthority(authority).get();
        } else
            return authorityRepo.save(new AuthorityEntity(authority));

    }

    @Transient
    private RoleEntity createRole(Role role, List<AuthorityEntity> authorities) {
        if (roleRepo.findByRole(role).isPresent())
            return roleRepo.findByRole(role).get();
        else {
            RoleEntity roleEntity = new RoleEntity(role);
            roleEntity.setAuthorities(authorities);
            return roleRepo.save(roleEntity);
        }
    }

}
