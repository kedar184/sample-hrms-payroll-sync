package io.abc.hrms.payroll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PayrollSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayrollSyncApplication.class, args);
    }
} 