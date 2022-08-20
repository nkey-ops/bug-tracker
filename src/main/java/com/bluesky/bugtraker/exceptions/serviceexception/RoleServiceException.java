package com.bluesky.bugtraker.exceptions.serviceexception;

import com.bluesky.bugtraker.exceptions.ErrorMessages;
import com.bluesky.bugtraker.shared.authorizationenum.Role;

import java.io.Serial;

public class RoleServiceException extends  ServiceException{
    @Serial
    private static final long serialVersionUID = 949632281436845943L;

    public RoleServiceException(String message) {
        super(message);
    }

    public RoleServiceException(ErrorMessages message) {
        super(message);
    }

    public RoleServiceException(ErrorMessages message, String identifier) {
        super(message, identifier);
    }
}
