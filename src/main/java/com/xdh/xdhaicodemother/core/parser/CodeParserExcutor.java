package com.xdh.xdhaicodemother.core.parser;

import com.xdh.xdhaicodemother.exception.BusinessException;
import com.xdh.xdhaicodemother.exception.ErrorCode;
import com.xdh.xdhaicodemother.model.enums.CodeGenTypeEnum;

/**
 * 代码解析策略执行器
 * 根据传入的类型执行对应的策略
 *
 * @author huanglina
 * date：  2026/7/29
 */
public class CodeParserExcutor {
    private static final MultiFileCodeParse multiFileCodeParse = new MultiFileCodeParse();
    private static final HtmlCodeParse htmlCodeParse = new HtmlCodeParse();


    /**
     * 执行代码解析
     *
     * @param codeContent     原始的内容
     * @param codeGenTypeEnum 代码生成类型
     * @return 解析后的代码对象（HtmlCodeResult 或 MultiFileCodeResult）
     */
    public static Object executeCodeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case MULTI_FILE -> multiFileCodeParse.codeParse(codeContent);
            case HTML -> htmlCodeParse.codeParse(codeContent);
            default ->
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码解析类型：" + codeGenTypeEnum.getValue());
        };
    }
}
