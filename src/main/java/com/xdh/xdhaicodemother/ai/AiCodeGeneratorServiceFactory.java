package com.xdh.xdhaicodemother.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
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

    @Resource
    private StreamingChatModel streamingChatModel;

    /*
      注册普通 AI 模型服务
      @return  普通 AI 模型服务
     */
//    @Bean
//    public AiCodeGeneratorService getAiCodeGeneratorService() {
//        return AiServices.create(AiCodeGeneratorService.class, chatModel);
//    }

    /**
     * 注册流式输出 AI 模型服务
     * @return  普通 AI 模型服务
     */
    @Bean
    public AiCodeGeneratorService getAiCodeGeneratorService() {
        return AiServices.builder(AiCodeGeneratorService.class)
                .streamingChatModel(streamingChatModel)
                .chatModel(chatModel)
                .build();
    }
}
