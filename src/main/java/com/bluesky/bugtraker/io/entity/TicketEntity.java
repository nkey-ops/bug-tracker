package com.bluesky.bugtraker.io.entity;


import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity(name = "Ticket")
@Table(name = "tickets")
@Getter
@Setter
public class TicketEntity implements Serializable {

    @Serial
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private static final long serialVersionUID = -2462587657644552577L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 30, unique = true)
    private String publicId;

    @Column(nullable = false)
    private String shortDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priority priority;

    @Lob
    @Column(nullable = false)
    private String howToReproduce;

    @Lob
    @Column(nullable = false)
    private String erroneousProgramBehaviour;

    @Lob
    @Column(columnDefinition = "varchar(250) default 'Solution is not found.'")
    private String howToSolve;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date reportedTime;

    @ManyToOne(optional = false,
            fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false, updatable = false)
    private UserEntity reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @OneToMany( mappedBy = "ticket",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<CommentEntity> comments = new HashSet<>();


    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(name = "tickets_users",
            joinColumns = {@JoinColumn(name = "ticket_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")}
    )
    private Set<UserEntity> ticketFixers;


    public boolean addBugFixer(UserEntity bugFixer) {
        boolean isAddedBugFixer =
                ticketFixers.add(bugFixer);
        boolean isAddedBug =
                bugFixer.getWorkingOnTickets().add(this);

        return isAddedBugFixer && isAddedBug;
    }

    public boolean removeBugFixer(UserEntity bugFixer) {
        boolean isRemovedBugFixer =
                ticketFixers.remove(bugFixer);
        boolean isRemovedBug =
                bugFixer.getWorkingOnTickets().remove(this);

        return isRemovedBugFixer && isRemovedBug;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketEntity ticketEntity = (TicketEntity) o;
        return Objects.equals(id, ticketEntity.id) && Objects.equals(publicId, ticketEntity.publicId) && Objects.equals(shortDescription, ticketEntity.shortDescription) && status == ticketEntity.status && severity == ticketEntity.severity && priority == ticketEntity.priority && Objects.equals(reportedTime, ticketEntity.reportedTime) && Objects.equals(reporter, ticketEntity.reporter) && Objects.equals(howToReproduce, ticketEntity.howToReproduce) && Objects.equals(erroneousProgramBehaviour, ticketEntity.erroneousProgramBehaviour) && Objects.equals(howToSolve, ticketEntity.howToSolve);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, publicId, shortDescription, status, severity, priority, reportedTime, reporter, howToReproduce, erroneousProgramBehaviour, howToSolve);
    }
}
