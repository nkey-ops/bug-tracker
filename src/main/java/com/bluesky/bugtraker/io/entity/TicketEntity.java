package com.bluesky.bugtraker.io.entity;

import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

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

  @Column(nullable = false, length = 5000)
  private String howToReproduce;

  @Column(nullable = false, length = 5000)
  private String erroneousProgramBehaviour;

  @Column(length = 5000)
  private String howToSolve;

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date createdTime;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastUpdateTime;

  @OneToMany(
      mappedBy = "mainTicket",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  private Set<TicketRecordEntity> ticketRecords = new HashSet<>();

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "reporter_id", nullable = false, updatable = false)
  private UserEntity reporter;

  @ManyToOne
  @JoinColumn(name = "project_id", referencedColumnName = "id", nullable = false, updatable = false)
  private ProjectEntity project;

  @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<CommentEntity> comments = new HashSet<>();

  @ManyToMany
  @JoinTable(
      name = "tickets_users",
      joinColumns = {@JoinColumn(name = "ticket_id")},
      inverseJoinColumns = {@JoinColumn(name = "user_id")})
  private Set<UserEntity> subscribers = new HashSet<>();

  public boolean setReporterEntity(UserEntity reporter) {
    Objects.requireNonNull(reporter);
    this.reporter = reporter;
    return reporter.getReportedTickets().add(this);
  }

  public boolean setProjectEntity(ProjectEntity project) {
    Objects.requireNonNull(project);
    boolean isAdded = project.getTickets().add(this); 
    this.project = project;

    return isAdded;
  }

  public boolean removeProjectEntity() {
    boolean isRemoved = project.getTickets().remove(this);
    this.project = null;

    return isRemoved;
  }

  public boolean addTicketRecord(TicketRecordEntity ticketRecordEntity) {
    boolean isTicketRecordEntityAdded = ticketRecords.add(ticketRecordEntity);

    ticketRecordEntity.setMainTicket(this);

    return isTicketRecordEntityAdded;
  }

  public boolean addSubscriber(UserEntity subscriber) {
    boolean isAddedSubscriber = subscribers.add(subscriber);
    boolean isAddedTicket = subscriber.getSubscribedToTickets().add(this);

    return isAddedSubscriber && isAddedTicket;
  }

  public boolean removeSubscriber(UserEntity subscriber) {
    boolean isRemovedSubscriber = subscribers.remove(subscriber);
    boolean isRemovedTicket = subscriber.getSubscribedToTickets().remove(this);

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
