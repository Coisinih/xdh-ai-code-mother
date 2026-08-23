package com.xdh.xdhaicodemother.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import com.xdh.xdhaicodemother.exception.ErrorCode;
import com.xdh.xdhaicodemother.exception.ThrowUtils;
import com.xdh.xdhaicodemother.manager.CosManager;
import com.xdh.xdhaicodemother.service.ScreenshotService;
import com.xdh.xdhaicodemother.utils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 截图相关服务
 *
 * @author huanglina
 * date：  2026/8/23
 */
@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {
    @Resource
    private CosManager cosManager;

    /**
     * 创建网页截图，并上传到cos对象存储
     *
     * @param webUrl 网页地址
     * @return 对象存储访问地址
     */
    public String generateAndUploadScreenshot(String webUrl) {
        ThrowUtils.throwIf(CharSequenceUtil.isBlank(webUrl), ErrorCode.OPERATION_ERROR, "webUrl不能为空");
        log.info("开始截图，Url:{}", webUrl);

        // 1.本地生成网页截图
        String localFilePath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        ThrowUtils.throwIf(CharSequenceUtil.isBlank(localFilePath), ErrorCode.OPERATION_ERROR, "生成网页截图失败。");

        try {
            // 2.上传到cos
            String cosUrl = uploadScreenshotToCos(localFilePath);
            ThrowUtils.throwIf(CharSequenceUtil.isBlank(cosUrl), ErrorCode.OPERATION_ERROR, "截图上传对象存储失败");
            log.info("网页截图生成并上传成功: {} -> {}", webUrl, cosUrl);
            return cosUrl;
        } finally {
            // 3.清理本地文件
            cleanupLocalFile(localFilePath);
        }
    }


    /**
     * 上传截图到对象存储
     *
     * @param localFilePath 本地截图路径
     * @return 对象存储访问URL，失败返回null
     */
    private String uploadScreenshotToCos(String localFilePath) {
        if (CharSequenceUtil.isBlank(localFilePath)) {
            return null;
        }
        File screenshotFile = FileUtil.file(localFilePath);
        if (!screenshotFile.exists()) {
            log.error("截图文件不存在: {}", localFilePath);
            return null;
        }
        String filename = RandomUtil.randomNumbers(8) + "_compress.jpg";
        String key = generateScreenshotKey(filename);
        return cosManager.uploadFile(key, screenshotFile);
    }

    /**
     * 生成截图的对象存储键
     * 格式：/screenshots/2025/07/31/filename.jpg
     */
    private String generateScreenshotKey(String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshots/%s/%s", datePath, fileName);
    }

    /**
     * 清理本地文件
     *
     * @param localFilePath 本地截图路径
     */
    private void cleanupLocalFile(String localFilePath) {
        File localFile = new File(localFilePath);
        if (localFile.exists()) {
            File parentFile = localFile.getParentFile();
            FileUtil.del(parentFile);
            log.info("本地截图文件已清理: {}", localFilePath);
        }
    }
}
