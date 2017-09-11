package com.yuandu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@MapperScan(basePackages="com.yuandu.user_service.dao.mapper")
public class APP {

    public static void main(String[] args) {
        SpringApplication.run(APP.class);
    }


}
