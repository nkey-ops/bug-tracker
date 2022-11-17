package com.bluesky.bugtraker.shared.authorizationenum;

import com.fasterxml.jackson.annotation.JsonFormat;

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
   
}
