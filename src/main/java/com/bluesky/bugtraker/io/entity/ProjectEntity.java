package com.bluesky.bugtraker.io.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "project")
@Table(name = "projects")
@Getter
@Setter
public class ProjectEntity implements Serializable {
  @Serial
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 30, unique = true)
  private String publicId;

  @Column(nullable = false, length = 30)
  private String name;

  @ManyToOne(optional = false)
  @JoinColumn(name = "creator_id", referencedColumnName = "id", nullable = false, updatable = false)
  private UserEntity creator;

  @OneToMany(
      mappedBy = "project",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  private Set<TicketEntity> tickets = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "projects_subscribers",
      joinColumns = @JoinColumn(name = "project_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "subscriber_id", referencedColumnName = "id"))
  private Set<UserEntity> subscribers = new HashSet<>();

  @OneToMany(
      mappedBy = "project",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private Set<CommentEntity> comments = new HashSet<>();

  public boolean setCreator(UserEntity creator) {
    this.creator = creator;
    return creator.getProjects().add(this);
  }

  public boolean removeCreator() {
    if (creator == null) return false;

    boolean isRemoved = creator.getProjects().remove(this);
    this.creator = null;

    return isRemoved;
  }

  public boolean addTicketEntity(TicketEntity ticketEntity) {
    Objects.requireNonNull(ticketEntity);
    boolean isAdded = tickets.add(ticketEntity);
    ticketEntity.setProject(this);

    return isAdded;
  }

  public boolean removeTicketEntity(TicketEntity ticketEntity) {
    boolean isRemoved = tickets.remove(ticketEntity);
    ticketEntity.setProject(null);

    return isRemoved;
  }

  public boolean addSubscriber(UserEntity subscriber) {
    boolean isAddedUser = subscribers.add(subscriber);
    boolean isAddedProject = subscriber.getSubscribedToProjects().add(this);

    return isAddedUser && isAddedProject;
  }

  public boolean removeSubscriber(UserEntity subscriber) {
    boolean isRemovedUser = subscribers.remove(subscriber);
    boolean isRemovedProject = subscriber.getSubscribedToProjects().remove(this);

    return isRemovedUser && isRemovedProject;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ProjectEntity that = (ProjectEntity) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
