package com.xdh.xdhaicodemother.core;

import com.xdh.xdhaicodemother.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

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

    @Test
    void generateAndSaveCodeStream() {
        // 原生 HTML 模式
        Flux<String> res = aiCodeGeneratorFacade.generateAndSaveCodeStream(CodeGenTypeEnum.HTML, "帮我生成一个登入页面，代码不超过20行。");
        List<String> block = res.collectList().block();
        Assertions.assertNotNull(block);
        Assertions.assertNotNull(String.join("", block));

        // 原生 多文件模式
        Flux<String> res1 = aiCodeGeneratorFacade.generateAndSaveCodeStream(CodeGenTypeEnum.MULTI_FILE, "帮我生成一个登入页面，代码总量不超过50行。");
        List<String> block1 = res1.collectList().block();
        Assertions.assertNotNull(block1);
        Assertions.assertNotNull(String.join("", block1));
    }
}