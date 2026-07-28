package com.xdh.xdhaicodemother.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.xdh.xdhaicodemother.ai.model.HtmlCodeResult;
import com.xdh.xdhaicodemother.ai.model.MultiFileCodeResult;
import com.xdh.xdhaicodemother.model.enums.CodeGenTypeEnum;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 文件保存
 *
 * @author huanglina
 * date：  2026/7/28
 */
@Service
public class CodeFileSave {
    // 文件保存的根目录 tmp/code_output
    private static final String FILE_SAVE_ROOT_PATH = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "code_output";

    /**
     * 保存 HtmlCodeResult 生成的的代码
     *
     * @param htmlCodeResult 单 html 生成结果
     * @return 生成的文件
     */
    public static File saveHtmlCodeResult(HtmlCodeResult htmlCodeResult) {
        String dirPath = createDirPath(CodeGenTypeEnum.HTML);
        writeToFile(dirPath, "index.html", htmlCodeResult.getHtmlCode());
        return new File(dirPath);
    }


    /**
     * 保存 MultiFileCodeResult 生成的的代码
     *
     * @param multiFileCodeResult 多文件
     * @return 生成的文件
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult multiFileCodeResult) {
        String dirPath = createDirPath(CodeGenTypeEnum.MULTI_FILE);
        writeToFile(dirPath, "index.html", multiFileCodeResult.getHtmlCode());
        writeToFile(dirPath, "style.ccs", multiFileCodeResult.getCssCode());
        writeToFile(dirPath, "script.js", multiFileCodeResult.getJsCode());
        return new File(dirPath);
    }


    /**
     * 创建唯一目录路径：CodeGenType_雪花id
     * exp：tmp/code_output/html_xxxxx
     *
     * @param codeGenType 生成文件类型
     * @return 目录路径
     */
    private static String createDirPath(CodeGenTypeEnum codeGenType) {
        // 目录名:html_xxxxx
        String uniqueDirName = StrUtil.format("{}_{}", codeGenType.getValue(), IdUtil.getSnowflakeNextIdStr());
        // 目录路径:tmp/code_output/html_xxxxx
        String dirPath = FILE_SAVE_ROOT_PATH + File.separator + uniqueDirName;
        // 创建目录
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入单个文件
     *
     * @param dirPath  目录路径
     * @param filename 文件名
     * @param content  内容
     */
    private static void writeToFile(String dirPath, String filename, String content) {
        String filePath = dirPath + File.separator + filename;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }
}
