package com.bluesky.bugtraker.service.specifications;

import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.TicketEntity;
import com.bluesky.bugtraker.io.entity.TicketRecordEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import java.util.Collection;

public class Specs {
    public static Specification<UserEntity> findAllUsersSubscribedToProject(final Long projectId){
        return (root, query, cb) -> {
            query.distinct(true);
            Root<ProjectEntity> project = query.from(ProjectEntity.class);
            Expression<Collection<UserEntity>> subscribers = project.get("subscribers");
            return cb.and(cb.equal(project.get("id"), projectId), cb.isMember(root, subscribers));
        };       
    }

    public static Specification<UserEntity> findAllUsersSubscribedToTicket(final Long ticketId){
        return (root, query, cb) -> {
            query.distinct(true);
            Root<TicketEntity> ticket = query.from(TicketEntity.class);
            Expression<Collection<UserEntity>> assignedDevs = ticket.get("assignedDevs");
            return cb.and(cb.equal(ticket.get("id"), ticketId), cb.isMember(root, assignedDevs));
        };       
    }

    public static Specification<ProjectEntity> findAllProjectsByCreatorId(final Long creatorId){
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.equal(root.get("creator").get("id"), creatorId);
        };       
    }

    public  static  Specification<TicketEntity> findAllByProjectId(final Long projectId){
        return (root, query, cb) -> {
            
            return cb.equal(root.get("project").get("id"), projectId);
        };
    }

    public  static  Specification<TicketRecordEntity> findAllTicketRecordsByTicketId(final Long ticketId){
        return (root, query, cb) -> {
            
            return cb.equal(root.get("mainTicket").get("id"), ticketId);
        };
    }
    
    
    
}
