package com.xdh.xdhaicodemother.ai;

import com.xdh.xdhaicodemother.ai.model.HtmlCodeResult;
import com.xdh.xdhaicodemother.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;
import org.springframework.stereotype.Component;

/**
 * AI 生成代码服务
 *
 * @author huanglina
 * date: 2026/7/28
 */
@Component
public interface AiCodeGeneratorService {

    /**
     * 生成 HTML 代码
     *
     * @param userMessage 用户消息
     * @return 生成的内容
     */
    @SystemMessage(fromResource = "prompts/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHTMLCode(String userMessage);


    /**
     * 生成 多文件 代码
     *
     * @param userMessage 用户消息
     * @return 生成的内容
     */
    @SystemMessage(fromResource = "prompts/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);
}
