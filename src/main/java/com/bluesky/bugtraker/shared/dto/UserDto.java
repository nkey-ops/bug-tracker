package com.bluesky.bugtraker.shared.dto;


import com.bluesky.bugtraker.io.entity.authorization.RoleEntity;
import com.bluesky.bugtraker.shared.authorizationenum.Role;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Getter
@Setter
public class UserDto implements Serializable {
    @Serial
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private static final long serialVersionUID = 2882079079986131255L;

    private Long id;
    private String publicId;
    private String username;
    private String email;
    private String password;


    private Set<ProjectDto> projects = new HashSet<>();
    private Set<TicketDto> reportedTickets = new HashSet<>();
    private Set<TicketDto> workingOnTickets = new HashSet<>();
    private Set<ProjectDto> subscribedToProjects = new HashSet<>();
    private Set<RoleEntity> roles = new HashSet<>();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDto userDto = (UserDto) o;
        return Objects.equals(publicId, userDto.publicId) && Objects.equals(username, userDto.username) && Objects.equals(email, userDto.email) && Objects.equals(password, userDto.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicId, username, email, password);
    }

}
