package com.xdh.xdhaicodemother.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.xdh.xdhaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.xdh.xdhaicodemother.model.entity.ChatHistory;
import com.xdh.xdhaicodemother.model.entity.User;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author xdh
 * @since 2026-08-01
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 保存对话消息
     * @param message   消息内容
     * @param userId     用户id
     * @param appId      应用id
     * @param messageType   消息类型 user/ai
     * @return  是否成功
     */
    boolean addChatMessage(String message, Long userId, Long appId, String messageType);

    boolean deleteByAppId(Long appId);

    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);
}
