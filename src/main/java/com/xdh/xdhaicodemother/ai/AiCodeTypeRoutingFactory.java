package com.xdh.xdhaicodemother.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 智能路由AI工厂
 *
 * @author huanglina
 * date：  2026/8/24
 */
@Configuration
public class AiCodeTypeRoutingFactory {
    @Resource
    private ChatModel chatModel;

    @Bean
    public AiCodeGenTypeRoutingService aiCodeTypeRoutingService() {
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(chatModel)
                .build();
    }

}
