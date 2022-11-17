package com.bluesky.bugtraker.security.accessevaluator;

import com.bluesky.bugtraker.service.impl.UserServiceImp;
import org.springframework.stereotype.Controller;

import javax.validation.constraints.NotNull;

@Controller
public class ProjectAccessEvaluator {
    
    private final UserServiceImp userServiceImp;

    public ProjectAccessEvaluator(UserServiceImp userServiceImp) {
        this.userServiceImp = userServiceImp;
    }

    public boolean removeSubscriber(@NotNull  String principalId, @NotNull String creatorId, 
                                    @NotNull String subscriberId){
        return creatorId.equals(principalId) || subscriberId.equals(principalId);
    }
}