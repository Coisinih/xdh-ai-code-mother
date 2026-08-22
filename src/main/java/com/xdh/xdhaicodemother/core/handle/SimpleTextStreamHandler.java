package com.xdh.xdhaicodemother.core.handle;

import cn.hutool.core.text.CharSequenceUtil;
import com.xdh.xdhaicodemother.model.entity.User;
import com.xdh.xdhaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.xdh.xdhaicodemother.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 简单文本流处理器
 * 处理 HTML 和 MULTI_FILE 类型的流式响应
 *
 * @author huanglina
 * date：  2026/8/21
 */
@Slf4j
public class SimpleTextStreamHandler {


    /**
     * 处理传统流（HTML, MULTI_FILE）
     * 直接收集完整的文本响应
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originFlux, ChatHistoryService chatHistoryService, User loginUser, Long appId) {
        StringBuilder resBuilder = new StringBuilder();
        return originFlux.map(chunk -> {
                    // 收集 AI 响应内容
                    resBuilder.append(chunk);
                    return chunk;
                })
                .doOnComplete(() -> {
                    // 流式返回结束后，保存 ai 消息
                    String aiResponse = resBuilder.toString();
                    if (CharSequenceUtil.isNotBlank(aiResponse)) {
                        chatHistoryService.addChatMessage(aiResponse, loginUser.getId(), appId, ChatHistoryMessageTypeEnum.AI.getValue());
                    }
                })
                .doOnError(e -> {
                    String errorMsg = "AI 回复失败：" + e.getMessage();
                    chatHistoryService.addChatMessage(errorMsg, loginUser.getId(), appId, ChatHistoryMessageTypeEnum.AI.getValue());
                });
    }
}
