package com.xdh.xdhaicodemother;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.xdh.xdhaicodemother.mapper")
public class XdhAiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(XdhAiCodeMotherApplication.class, args);
    }

}
