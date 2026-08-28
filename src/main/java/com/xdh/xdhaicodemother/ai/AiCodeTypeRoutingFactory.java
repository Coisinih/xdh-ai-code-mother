package com.xdh.xdhaicodemother.ai;

import com.xdh.xdhaicodemother.utils.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
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

    public AiCodeGenTypeRoutingService createAiCodeTypeRoutingService() {
        // 动态获取 路由模型 ，支持并发
        ChatModel chatModel = SpringContextUtil.getBean("routingChatModelPrototype", ChatModel.class);
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(chatModel)
                .build();
    }

    @Bean
    public AiCodeGenTypeRoutingService aiCodeTypeRoutingService() {
        return createAiCodeTypeRoutingService();
    }

}
