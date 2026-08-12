package com.campusgo.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.campusgo.infrastructure.persistence.jpa")
@EntityScan(basePackages = "com.campusgo.infrastructure.persistence.entity")
public class JpaConfig {
}
