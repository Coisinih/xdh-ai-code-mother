package com.xdh.xdhaicodemother.core;

import com.xdh.xdhaicodemother.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorFacadeTest {
    @Resource
    AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode() {
        // 原生 HTML 模式
        File file = aiCodeGeneratorFacade.generateAndSaveCode(CodeGenTypeEnum.HTML, "帮我生成一个登入页面，代码不超过20行。");
        Assertions.assertNotNull(file);

        // 原生 多文件模式
        File file1 = aiCodeGeneratorFacade.generateAndSaveCode(CodeGenTypeEnum.HTML, "帮我生成一个登入页面，代码总量不超过50行。");
        Assertions.assertNotNull(file1);
    }
}