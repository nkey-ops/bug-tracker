package com.bluesky.bugtraker.view.model.request;

import com.bluesky.bugtraker.shared.authorizationenum.Role;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.Size;

@Getter @Setter
public class UserRequestModel {
    @Size(min = 4, max = 20, 
            message = "Username length should be not less than 4 characters and no more than 20")
    private String username;
    
    @Size(max = 200, message = "Avatar URL length should be no more than 200")
    @URL(protocol = "https" , host = "i.imgur.com")
    
    private String avatarURL;
    @Size(max = 60, message = "Address length should be no more than 60")
    private String address;
    @Size(max = 12, message = "Phone number length should be no more than 12")
    private String phoneNumber;
    @Size(max = 12, message = "Status length should be no more than 12")
    private String status;
    
    private Role role;
}
