package com.bluesky.bugtraker.shared.authorizationenum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Role {
    ROLE_USER("User"), 
    ROLE_ADMIN("Admin"), 
    ROLE_SUPER_ADMIN("Super Admin");

    private final String text;
    
    Role(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getName() {
        return name();
    }
    
    @Override
    public String toString()
    {
        return text;
    }

    @JsonCreator
    public static Role forValues(@JsonProperty("text") String text,
                                     @JsonProperty("name") String name) {
        for (Role role : Role.values()) {
            if (role.getText().equals(text) && role.getName().equals(name)) 
                return role;
        }

        return null;
    }
   
}
