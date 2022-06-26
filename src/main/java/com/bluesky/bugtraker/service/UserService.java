package com.bluesky.bugtraker.service;


import com.bluesky.bugtraker.shared.UserDto;

public interface UserService {

    UserDto getUser(String id);
    UserDto updateUser(String id, UserDto userDto);
    UserDto createUser(UserDto userDto);
    void deleteUser(String id);
}
