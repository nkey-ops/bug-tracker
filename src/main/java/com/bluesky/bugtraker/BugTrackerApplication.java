package com.bluesky.bugtraker;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class BugTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(BugTrackerApplication.class, args);
    }


    @Bean
    public ModelMapper modelMapper(){
        return  new ModelMapper();
    }


}
