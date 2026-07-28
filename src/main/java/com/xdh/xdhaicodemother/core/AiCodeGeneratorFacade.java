package com.xdh.xdhaicodemother.core;

import com.xdh.xdhaicodemother.ai.AiCodeGeneratorService;
import com.xdh.xdhaicodemother.ai.model.HtmlCodeResult;
import com.xdh.xdhaicodemother.ai.model.MultiFileCodeResult;
import com.xdh.xdhaicodemother.exception.BusinessException;
import com.xdh.xdhaicodemother.exception.ErrorCode;
import com.xdh.xdhaicodemother.exception.ThrowUtils;
import com.xdh.xdhaicodemother.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * AI 代码生成外观类，组合代码生成和文件生成功能
 *
 * @author huanglina
 * date：  2026/7/28
 */
@Service
public class AiCodeGeneratorFacade {
    @Resource
    AiCodeGeneratorService aiCodeGeneratorService;


    /**
     * 统一 AI 生成代码入口，根据类型生成并保存代码文件
     *
     * @param codeGenTypeEnum 生成代码类型
     * @param userMessage     用户提示词
     * @return 生成的文件
     */
    public File generateAndSaveCode(CodeGenTypeEnum codeGenTypeEnum, String userMessage) {
        ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR, "生成类型为空");

        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
            default ->
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的生成类型：" + codeGenTypeEnum.getValue());
        };
    }

    /**
     * 生成 HTML 模式代码并保存
     *
     * @param userMessage   用户提示词
     * @return  生成的文件
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHTMLCode(userMessage);
        return CodeFileSave.saveHtmlCodeResult(htmlCodeResult);
    }


    /**
     * 生成 多文件模式 代码并保持
     *
     * @param userMessage 用户提示词
     * @return 生成的文件
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSave.saveMultiFileCodeResult(multiFileCodeResult);
    }
}
