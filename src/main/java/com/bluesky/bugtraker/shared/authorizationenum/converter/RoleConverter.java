package com.bluesky.bugtraker.shared.authorizationenum.converter;

import com.bluesky.bugtraker.shared.authorizationenum.Role;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String> {
 
    @Override
    public String convertToDatabaseColumn(Role role) {
        if (role == null) {
            return null;
        }
        return role.name();
    }

    @Override
    public Role convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }

        return Stream.of(Role.values())
          .filter(c -> c.name().equals(code))
          .findFirst()
          .orElseThrow(IllegalArgumentException::new);
    }
}