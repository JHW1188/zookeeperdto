package com.wjh.demoservice1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;


@ImportResource(locations={"classpath:bean.xml"})
@SpringBootApplication
public class DemoApp1 {
    public static void main(String[] args) {
        SpringApplication.run(DemoApp1.class);
    }
}