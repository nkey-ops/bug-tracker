package com.bluesky.bugtraker.io.entity.authorization;

import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.shared.authorizationenum.Role;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity(name = "roles")
@Getter @Setter
public class RoleEntity implements Serializable {

    @Serial
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private static final long serialVersionUID = 3895742807085645390L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, length = 30, unique = true)
    private Role role;

    @ManyToMany(mappedBy = "roles")
    private List<UserEntity> users =new ArrayList<>();

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "authority_id"))
    private Collection<AuthorityEntity> authorities = new ArrayList<>();

    public RoleEntity() {
    }

    public RoleEntity(Role role) {
        this.role = role;
    }
}
