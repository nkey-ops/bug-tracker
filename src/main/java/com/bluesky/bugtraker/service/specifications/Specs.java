package com.bluesky.bugtraker.service.specifications;

import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.TicketEntity;
import com.bluesky.bugtraker.io.entity.TicketRecordEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import java.time.Instant;
import java.time.Period;
import java.util.Collection;
import java.util.Date;

public class Specs {
    public static Specification<UserEntity> allProjectSubscribersByProject(final ProjectEntity projectEntity) {
        return (root, query, cb) -> {
            query.distinct(true);
            Root<ProjectEntity> project = query.from(ProjectEntity.class);
            Expression<Collection<UserEntity>> subscribers = project.get("subscribers");
            return cb.and(cb.equal(project, projectEntity), cb.isMember(root, subscribers));
        };
    }

    public static Specification<UserEntity> ticketSubscribersByTicket(final TicketEntity ticketEntity) {
        return (root, query, cb) -> {
            query.distinct(true);
            Root<TicketEntity> ticket = query.from(TicketEntity.class);
            Expression<Collection<UserEntity>> subscribers = ticket.get("subscribers");
            return cb.and(cb.equal(ticket, ticketEntity), cb.isMember(root, subscribers));
        };
    }

    public static Specification<ProjectEntity> projectByCreator(final UserEntity userEntity) {
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.equal(root.get("creator"), userEntity);
        };
    }

    public static Specification<TicketEntity> ticketByProject(final ProjectEntity projectEntity) {
        return (root, query, cb) -> cb.equal(root.get("project"), projectEntity);
    }

    public static Specification<TicketEntity> ticketByProjectCreator(final UserEntity creator) {
        return (root, query, cb) -> cb.equal(root.get("project").get("creator"), creator);
    }

    public static Specification<ProjectEntity> projectBySubscriber(UserEntity userEntity) {
        return (root, query, cb) -> {
            query.distinct(true);
            Root<ProjectEntity> project = query.from(ProjectEntity.class);
            Expression<Collection<UserEntity>> subscribers = project.get("subscribers");

            return cb.isMember(userEntity, subscribers);
        };
    }

    public static Specification<TicketEntity> ticketsUserSubscribedTo(UserEntity subscriber) {
        return (root, query, cb) -> {
            query.distinct(true);
            Root<TicketEntity> ticket = query.from(TicketEntity.class);
            Expression<Collection<UserEntity>> subscribers = ticket.get("subscribers");

            return cb.isMember(subscriber, subscribers);
        };
    }

    public static Specification<TicketRecordEntity> allTicketRecordsByTicket(TicketEntity ticketEntity) {
        return (root, query, cb) -> cb.equal(root.get("mainTicket"), ticketEntity);
    }

    public static Specification<TicketEntity> reportedADayAgo() {
        return (root, query, cb) -> 
                cb.between(root.get("createdTime"), Date.from(Instant.now().minus(Period.ofDays(1))), Date.from(Instant.now()));
    }

    public static Specification<TicketEntity> reportedAMonthAgo() {
        return (root, query, cb) -> {
            java.util.Date fromAMonth = Date.from(Instant.now().minus(Period.ofDays(30)));
            java.util.Date now = Date.from(Instant.now());
            return cb.between(root.get("createdTime"), fromAMonth, now);
        };
    }

    public static Specification<TicketEntity> statusIsCompleted() {
        return (root, query, cb) -> cb.equal(root.get("status"), Status.COMPLETED);
    }
    public static Specification<TicketEntity> statusIs(Status status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<TicketEntity> byReporter(UserEntity userEntity) {
        return (root, query, cb) -> cb.equal(root.get("reporter"), userEntity);
    }

    public static Specification<TicketEntity> bySubscriber(UserEntity userEntity) {
        return (root, query, cb) -> cb.isMember(userEntity, root.get("subscribers"));
    }

    public static Specification<TicketEntity> lastUpdatedWeeksAgo(int weeks) {
        Instant to = Instant.now().minus(Period.ofWeeks(weeks));
        Instant from = to.minus(Period.ofWeeks(weeks + 1));

        return (root, query, cb) -> cb.between(root.get("lastUpdateTime"), Date.from(from), Date.from(to));

    }

}