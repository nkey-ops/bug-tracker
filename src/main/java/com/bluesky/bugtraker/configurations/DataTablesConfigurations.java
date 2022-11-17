package com.bluesky.bugtraker.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.datatables.repository.DataTablesRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(repositoryFactoryBeanClass = DataTablesRepositoryFactoryBean.class, 
        basePackages = "com.bluesky.bugtraker.io.repository")
public class DataTablesConfigurations {
}
