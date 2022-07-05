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
@Getter @Setter
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

    @Column( length = 60)
    private String emailVerificationToken;

    @Column(nullable = false)
    private Boolean emailVerificationStatus = false;
// TODO check how bugEntity behaves when user is deleted

    @OneToMany(fetch = FetchType.EAGER,
            mappedBy = "reportedBy",
            cascade = CascadeType.PERSIST)
    private Set<BugEntity> reportedBugs;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "working_on_bugs")
    private Set<BugEntity> workingOnBugs;

    @OneToMany(fetch = FetchType.EAGER,
            cascade = CascadeType.REMOVE,
            mappedBy = "createdBy")
    private Set<ProjectEntity> projects;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable( name = "users_subscribed_to_projects")
    private Set<ProjectEntity> subscribedToProjects;


    @ManyToMany(fetch =  FetchType.EAGER)
    @JoinTable(name = "users_roles")
    private Set<RoleEntity> roles;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return id.equals(that.id) && publicId.equals(that.publicId) && email.equals(that.email) && encryptedPassword.equals(that.encryptedPassword) && userName.equals(that.userName) && Objects.equals(emailVerificationToken, that.emailVerificationToken) && emailVerificationStatus.equals(that.emailVerificationStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, publicId, email, encryptedPassword, userName, emailVerificationToken, emailVerificationStatus);
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
                ", bugs=" + reportedBugs +
                '}';
    }
}
