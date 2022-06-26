package com.bluesky.bugtraker.io.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity implements Serializable {
    static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private int privateId;

    @Column(nullable = false, length = 30)
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return privateId == that.privateId &&
                Objects.equals(publicId, that.publicId) &&
                Objects.equals(email, that.email) &&
                Objects.equals(encryptedPassword, that.encryptedPassword) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(emailVerificationToken, that.emailVerificationToken) &&
                Objects.equals(emailVerificationStatus, that.emailVerificationStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(privateId, publicId, email, encryptedPassword,
                userName, emailVerificationToken, emailVerificationStatus);
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "privateId=" + privateId +
                ", publicId='" + publicId + '\'' +
                ", email='" + email + '\'' +
                ", encryptedPassword='" + encryptedPassword + '\'' +
                ", userName='" + userName + '\'' +
                ", emailVerificationToken='" + emailVerificationToken + '\'' +
                ", emailVerificationStatus=" + emailVerificationStatus +
                '}';
    }
}
