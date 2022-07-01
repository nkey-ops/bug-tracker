package com.bluesky.bugtraker.io.entity.authorizationEntity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "authorities")
@Getter @Setter
public class AuthorityEntity implements Serializable {
    @Serial
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private static final long serialVersionUID = 4206589417849854824L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length =  30, unique = true)
    private String  name;

    @ManyToMany(mappedBy = "authorities")
    private Collection<RoleEntity> roles;

    public AuthorityEntity(String name) {
        this.name = name;
    }

    public AuthorityEntity() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



}