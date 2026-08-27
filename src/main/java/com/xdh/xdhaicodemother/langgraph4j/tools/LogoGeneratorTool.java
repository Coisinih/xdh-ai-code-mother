package com.xdh.xdhaicodemother.langgraph4j.tools;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdh.xdhaicodemother.langgraph4j.model.ImageResource;
import com.xdh.xdhaicodemother.langgraph4j.model.enums.ImageCategoryEnum;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Logo图片生成工具（使用的是 Token Plan 套餐，不能直接用 dashscope，只能通过发送 http 请求的方式调用大模型）
 *
 * @author xdh
 * @since 2026-08-27
 */
@Slf4j
@Component
public class LogoGeneratorTool {

    @Value("${dashscope.api-key:}")
    private String dashScopeApiKey;

    @Value("${dashscope.image-model:wan2.7-image}")
    private String imageModel;

    @Tool("根据描述生成 Logo 设计图片，用于网站品牌标识")
    public List<ImageResource> generateLogos(@P("Logo 设计描述，如名称、行业、风格等，尽量详细") String description) {
        List<ImageResource> logoList = new ArrayList<>();
        try {
            String logoPrompt = String.format("生成 Logo，Logo 中禁止包含任何文字！Logo 介绍：%s", description);

            // 使用 OkHttp 发送同步请求
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .build();

            // 构建请求体 - 严格按照 Token Plan 文档格式
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", imageModel);

            Map<String, Object> input = new HashMap<>();
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");

            List<Map<String, String>> content = new ArrayList<>();
            Map<String, String> textContent = new HashMap<>();
            textContent.put("text", logoPrompt);
            content.add(textContent);

            message.put("content", content);
            input.put("messages", Collections.singletonList(message));
            requestBody.put("input", input);

            Map<String, String> parameters = new HashMap<>();
            parameters.put("size", "1024*1024");
            requestBody.put("parameters", parameters);

            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(requestBody);

            // 使用正确的端点
            Request request = new Request.Builder()
                    .url("https://token-plan.cn-beijing.maas.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation")
                    .header("Authorization", "Bearer " + dashScopeApiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(jsonBody, MediaType.get("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    // 解析响应，提取图片 URL
                    Map<String, Object> resultMap = mapper.readValue(responseBody, Map.class);

                    // 根据文档：output.choices[*].message.content[*].image
                    Map<String, Object> output = (Map<String, Object>) resultMap.get("output");
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) output.get("choices");

                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> firstChoice = choices.get(0);
                        Map<String, Object> messageObj = (Map<String, Object>) firstChoice.get("message");
                        List<Map<String, Object>> contentList = (List<Map<String, Object>>) messageObj.get("content");

                        for (Map<String, Object> item : contentList) {
                            String imageUrl = (String) item.get("image");
                            if (StrUtil.isNotBlank(imageUrl)) {
                                logoList.add(ImageResource.builder()
                                        .category(ImageCategoryEnum.LOGO)
                                        .description(description)
                                        .url(imageUrl)
                                        .build());
                                log.info("Logo 生成成功，URL: {}", imageUrl);
                            }
                        }
                    }
                } else {
                    log.error("API 调用失败: {}", responseBody);
                }
            }

        } catch (Exception e) {
            log.error("生成 Logo 失败: {}", e.getMessage(), e);
        }
        return logoList;
    }
}

