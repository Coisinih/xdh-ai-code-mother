package com.xdh.xdhaicodemother.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.xdh.xdhaicodemother.constant.AppConstant;
import com.xdh.xdhaicodemother.exception.ErrorCode;
import com.xdh.xdhaicodemother.exception.ThrowUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 抽象代码文件保存器-模板方法模式
 *
 * @author huanglina
 * date：  2026/7/29
 */
public abstract class CodeFileSaverTemplate<T> {
    private static final String FILE_SAVE_ROOT_PATH = AppConstant.CODE_OUTPUT_ROOT_DIR;

    /**
     * 模板方法，保存代码的标准流程
     *
     * @param result 代码结果对象
     * @return 保存的目录文件
     */
    public final File saveCode(T result, Long appId) {
        // 1.输入校验
        doInputCheck(result);
        // 2.生成唯一路径
        String basePathDir = createDirPath(appId);

        // 3.保存文件
        saveCodeToFile(basePathDir, result);

        // 4.返回文件目录对象
        return new File(basePathDir);
    }

    /**
     * 基础参数校验
     */
    protected void doInputCheck(T result) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(result), ErrorCode.SYSTEM_ERROR, "生成文件内容不能为空");
    }

    /**
     * 创建唯一目录路径：CodeGenType_雪花id
     * exp：tmp/code_output/html_xxxxx
     *
     * @return 目录路径
     */
    private String createDirPath(Long appId) {
        // 目录名:html_xxxxx
        String uniqueDirName = StrUtil.format("{}_{}", getCodeType(), appId);
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
    protected static void writeToFile(String dirPath, String filename, String content) {
        String filePath = dirPath + File.separator + filename;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }


    /**
     * 保存文件（由子类实现）
     */
    protected abstract void saveCodeToFile(String basePathDir, T result);

    /**
     * 获取代码生成类型（由子类实现）
     *
     * @return 代码生成类型
     */
    protected abstract String getCodeType();
}
