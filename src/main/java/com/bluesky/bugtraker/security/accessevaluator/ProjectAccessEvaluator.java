package com.bluesky.bugtraker.security.accessevaluator;

import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Controller;

@Controller
public class ProjectAccessEvaluator {

    public boolean removeSubscriber(@NotNull  String principalId, @NotNull String creatorId, 
                                    @NotNull String subscriberId){
        return creatorId.equals(principalId) || subscriberId.equals(principalId);
    }
}