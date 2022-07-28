package com.bluesky.bugtraker.io.entity;

import com.bluesky.bugtraker.io.entity.authorization.RoleEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

@Entity(name = "User")
@Table(name = "users")
@Getter
@Setter
public class UserEntity implements Serializable {

    @Serial
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private static final long serialVersionUID = -3976585091175144574L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30, unique = true)
    private String publicId;

    @Column(nullable = false, length = 20)
    private String username;
    @Column(nullable = false, length = 320)
    private String email;

    @Column(nullable = false, length = 60)
    private String encryptedPassword;

    @Column(length = 60)
    private String emailVerificationToken;

    @Column(nullable = false)
    private Boolean emailVerificationStatus;

    @ManyToMany(mappedBy = "bugFixers",
            fetch = FetchType.LAZY)
    private Set<BugEntity> workingOnBugs;


    @ManyToMany(mappedBy = "subscribers",
            fetch = FetchType.LAZY)
    private Set<ProjectEntity> subscribedToProjects;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles")
    private Set<RoleEntity> roles;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(publicId, that.publicId) && Objects.equals(username, that.username) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, publicId, username, email);
    }
}
