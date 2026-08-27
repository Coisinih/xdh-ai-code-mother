package com.xdh.xdhaicodemother.langgraph4j.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 图片收集 AI 服务接口
 * 使用 AI 调用工具收集不同类型的图片资源
 *
 * @author xdh
 * @since 2026-08-27
 */
public interface ImageCollectionService {

    /**
     * 根据用户提示词收集所需的图片资源
     * AI 会根据需求自主选择调用相应的工具
     */
    @SystemMessage(fromResource = "prompts/image-collection-system-prompt.txt")
    String collectImages(@UserMessage String userPrompt);
}
