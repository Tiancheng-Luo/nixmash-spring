package com.nixmash.springdata.batch;

import com.nixmash.springdata.batch.wp.ImportConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
public class JobRunner {

    @Scheduled(fixedRate = 5000)
    public void findAndRunJob() {
        SpringApplication.run(ImportConfiguration.class);
    }

}
