package com.bluesky.bugtraker.io.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(name = "User")
@Table(name = "users")
@Getter @Setter
public class UserEntity implements Serializable {
    static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 30, unique = true)
    private String publicId;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(nullable = false, length = 60)
    private String encryptedPassword;

    @Column(nullable = false, length = 20)
    private String userName;

    private String emailVerificationToken;

    @Column(nullable = false)
    private Boolean emailVerificationStatus = false;
// TODO check how bugEntity behaves when user is deleted
    @OneToMany(
            mappedBy = "reportedBy",
            cascade = CascadeType.PERSIST)
// TODO change maybe to LinkedList
    private List<BugEntity> reportedBugs = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "working_on_bugs",
            joinColumns = @JoinColumn(name = "user_privateId"),
            inverseJoinColumns = @JoinColumn(name = "bug_id"))
    private List<BugEntity> workingOnBugs = new ArrayList<>();


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
