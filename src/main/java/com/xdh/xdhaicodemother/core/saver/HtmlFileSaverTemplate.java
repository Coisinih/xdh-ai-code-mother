package com.xdh.xdhaicodemother.core.saver;

import cn.hutool.core.util.StrUtil;
import com.xdh.xdhaicodemother.ai.model.HtmlCodeResult;
import com.xdh.xdhaicodemother.exception.ErrorCode;
import com.xdh.xdhaicodemother.exception.ThrowUtils;
import com.xdh.xdhaicodemother.model.enums.CodeGenTypeEnum;

/**
 * html 代码保存器
 *
 * @author huanglina
 * date：  2026/7/29
 */
public class HtmlFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {
    @Override
    protected void saveCodeToFile(String basePathDir, HtmlCodeResult htmlCodeResult) {
        writeToFile(basePathDir, "index.html", htmlCodeResult.getHtmlCode());
    }

    @Override
    protected String getCodeType() {
        return CodeGenTypeEnum.HTML.getValue();
    }

    @Override
    protected void doInputCheck(HtmlCodeResult result) {
        super.doInputCheck(result);
        ThrowUtils.throwIf(StrUtil.isBlank(result.getHtmlCode()), ErrorCode.SYSTEM_ERROR, "html 代码不能为空");
    }
}
