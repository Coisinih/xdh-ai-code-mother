package com.xdh.xdhaicodemother;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

// 允许使用缓存
@EnableCaching
@SpringBootApplication(exclude = RedisEmbeddingStoreAutoConfiguration.class)
@MapperScan("com.xdh.xdhaicodemother.mapper")
public class XdhAiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(XdhAiCodeMotherApplication.class, args);
    }

}
