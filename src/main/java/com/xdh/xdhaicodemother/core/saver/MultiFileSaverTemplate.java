package com.xdh.xdhaicodemother.core.saver;

import cn.hutool.core.util.StrUtil;
import com.xdh.xdhaicodemother.ai.model.MultiFileCodeResult;
import com.xdh.xdhaicodemother.exception.ErrorCode;
import com.xdh.xdhaicodemother.exception.ThrowUtils;
import com.xdh.xdhaicodemother.model.enums.CodeGenTypeEnum;

/**
 * 多文件代码保存器
 *
 * @author huanglina
 * date：  2026/7/29
 */
public class MultiFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {
    @Override
    protected void saveCodeToFile(String dirPath, MultiFileCodeResult multiFileCodeResult) {
        writeToFile(dirPath, "index.html", multiFileCodeResult.getHtmlCode());
        writeToFile(dirPath, "style.css", multiFileCodeResult.getCssCode());
        writeToFile(dirPath, "script.js", multiFileCodeResult.getJsCode());
    }

    @Override
    protected String getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE.getValue();
    }

    @Override
    protected void doInputCheck(MultiFileCodeResult result) {
        super.doInputCheck(result);
        // 至少要有 html 代码，ccs 和 js 代码可以没有
        ThrowUtils.throwIf(StrUtil.isBlank(result.getHtmlCode()), ErrorCode.SYSTEM_ERROR, "html 代码不能为空");
    }
}
