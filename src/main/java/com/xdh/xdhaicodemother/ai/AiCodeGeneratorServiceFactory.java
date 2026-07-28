package com.xdh.xdhaicodemother.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 服务创建工厂类
 *
 * @author huanglina
 * date：  2026/7/28
 */

// Spring Boot 启动时扫描注解自动创建 bean 对象
@Configuration
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Bean
    public AiCodeGeneratorService getAiCodeGeneratorService() {
        return AiServices.create(AiCodeGeneratorService.class, chatModel);
    }
}
