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
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Lob
    @Column(nullable = false)
    private String howToReproduce;

    @Lob
    @Column(nullable = false)
    private String erroneousProgramBehaviour;

    @Lob
    @Column
    private String howToSolve;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdateTime;

    @OneToMany(mappedBy = "mainTicket",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    private Set<TicketRecordEntity> ticketRecords = new HashSet<>();

    @ManyToOne(optional = false,
            fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false, updatable = false)
    private UserEntity reporter;
    @ManyToOne
    @JoinColumn(name="project_id", 
                referencedColumnName = "id",
                nullable = false, updatable = false)
    private ProjectEntity project;
    
    @OneToMany(mappedBy = "ticket",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<CommentEntity> comments = new HashSet<>();
    @ManyToMany
    @JoinTable(name = "tickets_users",
            joinColumns = {@JoinColumn(name = "ticket_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    private Set<UserEntity> subscribers = new HashSet<>();
  
    
    
    public boolean addTicketRecord(TicketRecordEntity ticketRecordEntity) {
        boolean isTicketRecordEntityAdded =
                ticketRecords.add(ticketRecordEntity);
                
        ticketRecordEntity.setMainTicket(this);
        
        return isTicketRecordEntityAdded;
    }
    
    public boolean addSubscriber(UserEntity subscriber) {
        boolean isAddedSubscriber =
                subscribers.add(subscriber);
        boolean isAddedTicket =
                subscriber.getSubscribedToTickets().add(this);

        return isAddedSubscriber && isAddedTicket;
    }

    public boolean removeSubscriber(UserEntity subscriber) {
        boolean isRemovedSubscriber =
                subscribers.remove(subscriber);
        boolean isRemovedTicket =
                subscriber.getSubscribedToTickets().remove(this);

        return isRemovedSubscriber && isRemovedTicket;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketEntity that = (TicketEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
