package com.bluesky.bugtraker.security.accessevaluator;

import org.springframework.stereotype.Controller;

import com.bluesky.bugtraker.service.impl.UserServiceImp;

import jakarta.validation.constraints.NotNull;

@Controller
public class TicketAccessEvaluator {
    
    private final UserServiceImp userServiceImp;

    public TicketAccessEvaluator(UserServiceImp userServiceImp) {
        this.userServiceImp = userServiceImp;
    }

    public boolean areCommentsAllowed(@NotNull  String principalId, @NotNull String creatorId, @NotNull String ticketId){
        return creatorId.equals(principalId) || userServiceImp.isSubscribedToTicket(principalId, ticketId);
    }
    
    public boolean isTicketEditionAllowed(@NotNull  String principalId, @NotNull String creatorId, @NotNull String ticketId){
        return  creatorId.equals(principalId) || userServiceImp.isSubscribedToTicket(principalId, ticketId);        
    }

    public boolean isUserSubscribed(@NotNull String principalId,@NotNull String ticketId){
        return  userServiceImp.isSubscribedToTicket(principalId, ticketId);        
    }
}
