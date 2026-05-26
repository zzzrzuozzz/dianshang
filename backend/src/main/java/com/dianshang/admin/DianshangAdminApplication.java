package com.dianshang.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DianshangAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(DianshangAdminApplication.class, args);
    }
}
