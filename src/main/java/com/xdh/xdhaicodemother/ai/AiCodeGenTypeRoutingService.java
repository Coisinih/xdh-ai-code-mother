package com.xdh.xdhaicodemother.ai;

import com.xdh.xdhaicodemother.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;
import org.springframework.stereotype.Component;

/**
 * 代码生成类型路由
 *
 * @author huanglina
 * date: 2026/8/24
 */
@Component
public interface AiCodeGenTypeRoutingService {
    /**
     * 根据用户需求返回最合适的代码生成类型
     *
     * @param userDemand 用户需求
     * @return 代码生成类型
     */
    @SystemMessage(fromResource = "prompts/codegen-routing-system-prompt.txt")
    CodeGenTypeEnum routeCodeGenType(String userDemand);
}
