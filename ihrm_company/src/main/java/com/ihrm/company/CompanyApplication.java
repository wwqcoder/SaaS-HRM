package com.ihrm.company;

import com.ihrm.common.utils.IdWorker;
import com.ihrm.common.utils.JwtUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

/**
 * @author: Mr.Wang
 * @create: 2019-08-31 10:54
 **/
@SpringBootApplication(scanBasePackages = "com.ihrm")
@EntityScan("com.ihrm")
@EnableEurekaClient
public class CompanyApplication {
    public static void main(String[] args) {
        SpringApplication.run(CompanyApplication.class, args);
    }
    @Bean
    public IdWorker idWorkker() {
        return new IdWorker(1, 1);
   }

   @Bean
    public JwtUtils jwtUtils(){
        return new JwtUtils();
   }
}
