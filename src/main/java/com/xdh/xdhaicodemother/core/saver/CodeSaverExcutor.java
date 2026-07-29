package com.xdh.xdhaicodemother.core.saver;

import com.xdh.xdhaicodemother.ai.model.HtmlCodeResult;
import com.xdh.xdhaicodemother.ai.model.MultiFileCodeResult;
import com.xdh.xdhaicodemother.exception.BusinessException;
import com.xdh.xdhaicodemother.exception.ErrorCode;
import com.xdh.xdhaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 代码文件保存执行器
 * 根据代码生成类型执行对应的逻辑
 *
 * @author huanglina
 * date：  2026/7/29
 */
public class CodeSaverExcutor {

    private static final HtmlFileSaverTemplate htmlFileSaverTemplate = new HtmlFileSaverTemplate();
    private static final MultiFileSaverTemplate multiFileSaverTemplate = new MultiFileSaverTemplate();

    public static File codeFileSaver(Object result, CodeGenTypeEnum codeType) {


        return switch (codeType) {
            case HTML -> htmlFileSaverTemplate.saveCode((HtmlCodeResult) result);
            case MULTI_FILE -> multiFileSaverTemplate.saveCode((MultiFileCodeResult) result);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码类型");
        };
    }
}
