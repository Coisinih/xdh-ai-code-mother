package com.xdh.xdhaicodemother.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WebScreenshotUtilsTest {

    @Test
    void saveWebPageScreenshot() {
        String pageScreenshot = WebScreenshotUtils.saveWebPageScreenshot("https://www.baidu.com");
        Assertions.assertNotNull(pageScreenshot);
    }
}