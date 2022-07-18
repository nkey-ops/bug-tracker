package com.bluesky.bugtraker.io.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "projects")
@Getter
@Setter
public class ProjectEntity implements Serializable {
    @Serial
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private static final long serialVersionUID = 90481049740702283L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30, unique = true)
    private String publicId;
    @Column(nullable = false, length = 30)
    private String name;

    @ManyToOne(optional = false,
               fetch =  FetchType.LAZY)
    @JoinColumn(name="creator_id",
                nullable=false,
                updatable=false)
    private UserEntity creator;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<BugEntity> bugs;

    @ManyToMany
    @JoinTable(name = "subscribers_projects")
    private Set<UserEntity> subscribers;

    public boolean addBug(BugEntity bugEntity) {
        boolean isAdded = bugs.add(bugEntity);

        if (isAdded) bugEntity.setProject(this);

        return isAdded;
    }

    public boolean removeBug(BugEntity bugEntity) {
        boolean isRemoved = bugs.remove(bugEntity);

        if (isRemoved) bugEntity.setProject(null);

        return isRemoved;
    }
    public boolean addSubscriber(UserEntity userEntity) {
        boolean isAddedUser = subscribers.add(userEntity);

        boolean isAddedProject =
            userEntity.getSubscribedToProjects().add(this);

        return isAddedUser && isAddedProject;
    }

    public boolean removeSubscriber(UserEntity userEntity) {
        boolean isRemovedUser = subscribers.remove(userEntity);

        boolean isRemovedProject =
                userEntity.getSubscribedToProjects().remove(this);

        return isRemovedUser && isRemovedProject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectEntity that = (ProjectEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(publicId, that.publicId) && Objects.equals(name, that.name);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, publicId, name);
    }
}