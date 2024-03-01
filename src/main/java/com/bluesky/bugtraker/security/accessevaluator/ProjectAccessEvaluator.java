package com.bluesky.bugtraker.security.accessevaluator;

import org.springframework.stereotype.Controller;

import com.bluesky.bugtraker.service.impl.UserServiceImp;

import jakarta.validation.constraints.NotNull;

import org.springframework.stereotype.Controller;

@Controller
public class ProjectAccessEvaluator {

    public boolean removeSubscriber(@NotNull  String principalId, @NotNull String creatorId, 
                                    @NotNull String subscriberId){
        return creatorId.equals(principalId) || subscriberId.equals(principalId);
    }
}
