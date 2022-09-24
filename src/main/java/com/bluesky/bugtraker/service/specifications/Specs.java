package com.bluesky.bugtraker.service.specifications;

import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.TicketEntity;
import com.bluesky.bugtraker.io.entity.TicketRecordEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import java.sql.Date;
import java.time.Instant;
import java.time.Period;
import java.util.Collection;

public class Specs {
    public static Specification<UserEntity> findAllUsersSubscribedToProject(final Long projectId) {
        return (root, query, cb) -> {
            query.distinct(true);
            Root<ProjectEntity> project = query.from(ProjectEntity.class);
            Expression<Collection<UserEntity>> subscribers = project.get("subscribers");
            return cb.and(cb.equal(project.get("id"), projectId), cb.isMember(root, subscribers));
        };
    }

    public static Specification<UserEntity> findAllUsersSubscribedToTicket(final Long ticketId) {
        return (root, query, cb) -> {
            query.distinct(true);
            Root<TicketEntity> ticket = query.from(TicketEntity.class);
            Expression<Collection<UserEntity>> assignedDevs = ticket.get("assignedDevs");
            return cb.and(cb.equal(ticket.get("id"), ticketId), cb.isMember(root, assignedDevs));
        };
    }

    public static Specification<ProjectEntity> findAllProjectsByCreatorId(final Long creatorId) {
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.equal(root.get("creator").get("id"), creatorId);
        };
    }

    public static Specification<TicketEntity> byProjectId(final Long projectId) {
        return (root, query, cb) -> {

            return cb.equal(root.get("project").get("id"), projectId);
        };
    }

    public static Specification<TicketRecordEntity> findAllTicketRecordsByTicketId(final Long ticketId) {
        return (root, query, cb) -> {

            return cb.equal(root.get("mainTicket").get("id"), ticketId);
        };
    }

    public static Specification<TicketEntity> reportedADayAgo() {
        return (root, query, cb) -> {
            return cb.between(root.get("reportedTime"), Date.from(Instant.now().minus(Period.ofDays(1))), Date.from(Instant.now()));
        };
    }
    public static Specification<TicketEntity> reportedAMonthAgo() {
        return (root, query, cb) -> {
            java.util.Date fromAMonth = Date.from(Instant.now().minus(Period.ofDays(30)));
            java.util.Date now = Date.from(Instant.now());
            return cb.between(root.get("reportedTime"),fromAMonth, now);
        };
    }

    public static Specification<TicketEntity> severityIsCritical() {
        return (root, query, cb) -> {
            return cb.equal(root.get("severity"), Severity.CRITICAL);
        };
    }

    public static Specification<TicketEntity> statusIsCompleted() {
        return (root, query, cb) -> {
            return cb.equal(root.get("status"), Status.COMPLETED);
        };
    }

    public static Specification<TicketEntity> statusIsInProgress() {
        return (root, query, cb) -> {
            return cb.equal(root.get("status"), Status.IN_PROGRESS);
        };
    }

    public static Specification<TicketEntity> byReporter(UserEntity userEntity) {
        return (root, query, cb) -> {
            return cb.equal(root.get("reporter"), userEntity);
        };
    }
    public static Specification<TicketEntity> byAssigndedDev(UserEntity userEntity) {
        return (root, query, cb) -> {
            return cb.isMember(userEntity,  root.get("assignedDevs"));
        };
    }
    


}