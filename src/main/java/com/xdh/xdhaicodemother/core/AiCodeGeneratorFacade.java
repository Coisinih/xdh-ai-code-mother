package com.xdh.xdhaicodemother.core;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.xdh.xdhaicodemother.ai.AiCodeGeneratorService;
import com.xdh.xdhaicodemother.ai.model.HtmlCodeResult;
import com.xdh.xdhaicodemother.ai.model.MultiFileCodeResult;
import com.xdh.xdhaicodemother.core.parser.CodeParserExcutor;
import com.xdh.xdhaicodemother.core.saver.CodeSaverExcutor;
import com.xdh.xdhaicodemother.exception.BusinessException;
import com.xdh.xdhaicodemother.exception.ErrorCode;
import com.xdh.xdhaicodemother.exception.ThrowUtils;
import com.xdh.xdhaicodemother.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成外观类，组合代码生成和文件生成功能
 *
 * @author huanglina
 * date：  2026/7/28
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {
    @Resource
    AiCodeGeneratorService aiCodeGeneratorService;


    /**
     * 统一 AI 生成代码入口，根据类型生成并保存代码文件
     *
     * @param codeGenType 生成代码类型
     * @param userMessage 用户提示词
     * @return 生成的文件
     */
    public File generateAndSaveCode(CodeGenTypeEnum codeGenType, String userMessage, Long appId) {
        ThrowUtils.throwIf(codeGenType == null, ErrorCode.PARAMS_ERROR, "生成类型为空");

        return switch (codeGenType) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHTMLCode(userMessage);
                yield CodeSaverExcutor.codeFileSaver(htmlCodeResult, codeGenType, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeSaverExcutor.codeFileSaver(multiFileCodeResult, codeGenType, appId);
            }
            default ->
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的生成类型：" + codeGenType.getValue());
        };
    }

    /**
     * 统一 AI 生成代码入口，根据类型生成并保存代码文件（流式）
     *
     * @param codeGenType 生成代码类型
     * @param userMessage 用户提示词
     * @return 生成的文件
     */
    public Flux<String> generateAndSaveCodeStream(CodeGenTypeEnum codeGenType, String userMessage, Long appId) {
        ThrowUtils.throwIf(codeGenType == null, ErrorCode.PARAMS_ERROR, "生成类型为空");

        return switch (codeGenType) {
            case HTML -> {
                Flux<String> res = aiCodeGeneratorService.generateHTMLCodeStream(userMessage);
                yield processCodeStream(res, codeGenType, appId);
            }
            case MULTI_FILE -> {
                Flux<String> res = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(res, codeGenType, appId);
            }
            default ->
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的生成类型：" + codeGenType.getValue());
        };
    }

    /**
     * 从模型返回内容中解析出代码，并保存
     *
     * @param res         生成
     * @param codeGenType 代码生成类型
     */
    @SuppressWarnings("all")
    private Flux<String> processCodeStream(Flux<String> res, CodeGenTypeEnum codeGenType, Long appId) {
        StringBuilder sb = new StringBuilder();
        return res.doOnNext(sb::append)
                .doOnComplete(() -> {
                    try {
                        // 全部输出完成后
                        // 调用解析执行器解析出代码内容
                        Object codeObject = CodeParserExcutor.executeCodeParser(sb.toString(), codeGenType);
                        // 调用代码文件保存执行器，将代码内容保存为文件
                        File file = CodeSaverExcutor.codeFileSaver(codeObject, codeGenType, appId);
                        log.info("保存成功，路径为：{}", file.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存失败:{}", ExceptionUtil.stacktraceToString(e));
                    }
                });
    }


    /*
     * ----------------------------以下已经统一抽象封装为上面的方法---------------------------------------------------
     */

    /**
     * 生成 HTML 模式代码并保存（流式）
     *
     * @param userMessage 用户提示词
     * @return 生成的文件
     * @deprecated
     */
    @SuppressWarnings("all")
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
        Flux<String> res = aiCodeGeneratorService.generateHTMLCodeStream(userMessage);
        StringBuilder sb = new StringBuilder();
        return res.doOnNext(sb::append)
                .doOnComplete(() -> {
                    try {
                        // 全部输出完成后，解析内容并保存到文件中
                        HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(sb.toString());
                        File file = CodeFileSave.saveHtmlCodeResult(htmlCodeResult);
                        log.info("保存成功，路径为：{}", file.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存失败:{}", ExceptionUtil.stacktraceToString(e));
                    }
                });
    }

    /**
     * 生成 多文件模式 代码并保持（流式）
     *
     * @param userMessage 用户提示词
     * @return 生成的文件
     * @deprecated
     */
    @SuppressWarnings("all")
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
        Flux<String> res = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
        StringBuilder sb = new StringBuilder();
        return res.doOnNext(sb::append)
                .doOnComplete(() -> {
                    try {
                        // 全部输出完成后，解析内容并保存到文件中
                        MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(sb.toString());
                        File file = CodeFileSave.saveMultiFileCodeResult(multiFileCodeResult);
                        log.info("保存成功，路径为：{}", file.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存失败:{}", ExceptionUtil.stacktraceToString(e));
                    }
                });
    }

    /**
     * 生成 HTML 模式代码并保存
     *
     * @param userMessage 用户提示词
     * @return 生成的文件
     */
    @SuppressWarnings("all")
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
    @SuppressWarnings("all")
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSave.saveMultiFileCodeResult(multiFileCodeResult);
    }
}
