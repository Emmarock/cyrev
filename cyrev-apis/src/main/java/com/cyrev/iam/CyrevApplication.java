package com.cyrev.iam;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication(scanBasePackages = {"com.cyrev"})
@EnableJpaRepositories(basePackages = {"com.cyrev.common.repository"})
@EntityScan(basePackages = {"com.cyrev.common.entities"})
public class CyrevApplication {
 public static void main(String[] args){
  SpringApplication.run(CyrevApplication.class,args);
 }
}
