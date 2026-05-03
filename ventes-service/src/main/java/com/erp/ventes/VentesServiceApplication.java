package com.erp.ventes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class VentesServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VentesServiceApplication.class, args);
    }
}
