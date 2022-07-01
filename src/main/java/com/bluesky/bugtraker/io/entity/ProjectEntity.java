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
@Getter @Setter
public class ProjectEntity implements Serializable {
    @Serial
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private static final long serialVersionUID = 90481049740702283L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;
    @ManyToOne
    @JoinColumn(nullable = false)
    private UserEntity createdBy;

    @OneToMany
    private Set<BugEntity> bugs;


    @ManyToMany(mappedBy = "subscribedToProjects", fetch = FetchType.EAGER)
    private Set<UserEntity> subscribers;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectEntity that = (ProjectEntity) o;
        return id.equals(that.id) &&
                name.equals(that.name) &&
                createdBy.equals(that.createdBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, createdBy);
    }
}