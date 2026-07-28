package com.xdh.xdhaicodemother.ai;

import com.xdh.xdhaicodemother.ai.model.HtmlCodeResult;
import com.xdh.xdhaicodemother.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    AiCodeGeneratorService aiCodeGeneratorService;


    @Test
    void generateHTMLCode() {
        HtmlCodeResult res = aiCodeGeneratorService.generateHTMLCode("创建一个咸蛋黄的主页，代码不超过20行。");
        Assertions.assertNotNull(res);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult res = aiCodeGeneratorService.generateMultiFileCode("创建一个咸蛋黄的留言板，要求50行代码简易版。");
        Assertions.assertNotNull(res);
    }
}