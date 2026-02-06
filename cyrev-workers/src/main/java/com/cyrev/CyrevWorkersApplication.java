package com.cyrev;

import com.cyrev.common.annotations.TemporalWorkflow;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan(
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION,
                classes = TemporalWorkflow.class
        )
)
@SpringBootApplication(scanBasePackages = {"com.cyrev"})
@EnableJpaRepositories(basePackages = {"com.cyrev.common.repository"})
@EntityScan(basePackages = {"com.cyrev.common.entities"})
public class CyrevWorkersApplication {
    public static void main(String[] args) {
        SpringApplication.run(CyrevWorkersApplication.class, args);
    }
}