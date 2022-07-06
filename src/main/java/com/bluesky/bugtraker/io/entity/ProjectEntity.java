package com.bluesky.bugtraker.io.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @Column(nullable = false, length = 30, unique = true)
    private String publicId;
    @Column(nullable = false, length =30)
    private String name;

    @ManyToOne
    @JoinColumn(nullable = false)
    private UserEntity creator;

    @OneToMany(fetch = FetchType.EAGER,
                cascade = CascadeType.REMOVE,
                mappedBy = "project")
    private Set<BugEntity> bugs;

    @ManyToMany(mappedBy = "subscribedToProjects", fetch = FetchType.EAGER)
    private Set<UserEntity> subscribers;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectEntity that = (ProjectEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(creator, that.creator) && Objects.equals(bugs, that.bugs) && Objects.equals(subscribers, that.subscribers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, creator, bugs, subscribers);
    }
}