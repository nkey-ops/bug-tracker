package com.bluesky.bugtraker.io.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity(name = "user")
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
    
    @Column(length = 200)
    private String avatarURL;
    @Column(length = 60)
    private String address;
    @Column(length = 12)
    private String phoneNumber;
    @Column(length = 80)
    private String status;

    @Column(nullable = false, length = 60)
    private String encryptedPassword;

    @Column(length = 60)
    private String emailVerificationToken;

    @Column(nullable = false)
    private Boolean emailVerificationStatus;

    @ManyToOne( fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "users_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "roles_id", referencedColumnName = "id"))
    private RoleEntity roleEntity;


    @OneToMany(mappedBy = "creator", 
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    private Set<ProjectEntity> projects = new HashSet<>();
    
    @ManyToMany(mappedBy = "subscribers",
            fetch = FetchType.LAZY)
    private Set<ProjectEntity> subscribedToProjects = new HashSet<>();


    @OneToMany(
            cascade = CascadeType.ALL)
    private Set<TicketEntity> reportedTickets = new HashSet<>();

    @ManyToMany(mappedBy = "subscribers",
            fetch = FetchType.LAZY)
    private Set<TicketEntity> subscribedToTickets = new HashSet<>();

    @OneToMany(mappedBy = "creator",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    private Set<CommentEntity> comments = new HashSet<>();

    public boolean setRoleEntity(RoleEntity roleEntity) {
        this.roleEntity = roleEntity;
        return roleEntity.getUsers().add(this);
    }

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
