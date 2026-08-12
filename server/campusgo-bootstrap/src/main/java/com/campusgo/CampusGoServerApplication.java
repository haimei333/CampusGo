package com.campusgo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.campusgo")
public class CampusGoServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusGoServerApplication.class, args);
    }
}
