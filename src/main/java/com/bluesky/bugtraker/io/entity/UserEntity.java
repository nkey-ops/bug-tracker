package com.bluesky.bugtraker.io.entity;

import com.bluesky.bugtraker.io.entity.authorization.RoleEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;

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
    private String userName;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(nullable = false, length = 60)
    private String encryptedPassword;

    @Column(length = 60)
    private String emailVerificationToken;

    @Column(nullable = false)
    private Boolean emailVerificationStatus = false;

//    @OneToMany(
//            mappedBy = "creator",
//            cascade = CascadeType.ALL,
//            orphanRemoval = true
//    )
//    private Set<ProjectEntity> projects;
// findBy createdBy

    @OneToMany(mappedBy = "reportedBy",
               cascade = CascadeType.PERSIST)
    private Set<BugEntity> reportedBugs;
// findBy reportedBy

    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(name = "working_on_bugs")
    private Set<BugEntity> workingOnBugs;


    @ManyToMany(mappedBy = "subscribers")
    private Set<ProjectEntity> subscribedToProjects;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles")
    private Set<RoleEntity> roles;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(publicId, that.publicId) && Objects.equals(userName, that.userName) && Objects.equals(email, that.email) && Objects.equals(encryptedPassword, that.encryptedPassword) && Objects.equals(emailVerificationToken, that.emailVerificationToken) && Objects.equals(emailVerificationStatus, that.emailVerificationStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, publicId, userName, email, encryptedPassword, emailVerificationToken, emailVerificationStatus);
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "privateId=" + id +
                ", publicId='" + publicId + '\'' +
                ", email='" + email + '\'' +
                ", encryptedPassword='" + encryptedPassword + '\'' +
                ", userName='" + userName + '\'' +
                ", emailVerificationToken='" + emailVerificationToken + '\'' +
                ", emailVerificationStatus=" + emailVerificationStatus +
                '}';
    }
}
