package com.bluesky.bugtraker.io.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Comment")
@Table(name = "comments")
@Getter
@Setter
public class CommentEntity implements Serializable {

  @Serial
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private static final long serialVersionUID = 2807970031426195256L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 30, unique = true)
  private String publicId;

  @Column(nullable = false, length = 1000)
  private String content;

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date uploadTime;

  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, updatable = false)
  private UserEntity creator;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinTable(
      name = "tickets_comments",
      joinColumns = @JoinColumn(name = "comment_id"),
      inverseJoinColumns = @JoinColumn(name = "ticket_id"))
  private TicketEntity ticket;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinTable(
      name = "tickets_projects",
      joinColumns = @JoinColumn(name = "project_id"),
      inverseJoinColumns = @JoinColumn(name = "ticket_id"))
  private ProjectEntity project;

  public boolean setCreator(UserEntity creator) {
    boolean isCommentAdded = creator.getComments().add(this);
    this.creator = creator;

    return isCommentAdded;
  }

  public boolean addTicket(TicketEntity ticketEntity) {
    boolean isTicketAdded = ticketEntity.getComments().add(this);
    this.setTicket(ticketEntity);

    return isTicketAdded;
  }

  public boolean addProject(ProjectEntity projectEntity) {
    boolean isProjectAdded = projectEntity.getComments().add(this);
    this.setProject(projectEntity);

    return isProjectAdded;
  }
}
