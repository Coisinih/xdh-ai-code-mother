package com.xdh.xdhaicodemother.langgraph4j.tools;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xdh.xdhaicodemother.langgraph4j.model.ImageResource;
import com.xdh.xdhaicodemother.langgraph4j.model.enums.ImageCategoryEnum;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 内容图片搜索工具类
 * <a href="https://https://www.pexels.com/api/documentation/">接口文档</a>
 *
 * @author xdh
 * @since 2026-08-27
 */
@Slf4j
@Component
public class ImageSearchTool {

    private static final String PEXELS_API_URL = "https://api.pexels.com/v1/search";

    @Value("${pexels.api-key}")
    private String pexelsApiKey;


    @Tool("搜索内容相关的图片，用于网站内容展示")
    public List<ImageResource> searchContentImages(@P("搜索关键词") String query) {
        List<ImageResource> imageList = new ArrayList<>();
        int searchCount = 10;
        // 使用 try-with-resources 自动释放 HTTP 资源
        // curl -H "Authorization: YOUR_API_KEY" \ "https://api.pexels.com/v1/search?query=nature&per_page=1"
        try (HttpResponse response = HttpRequest.get(PEXELS_API_URL)
                .header("Authorization", pexelsApiKey)
                .form("query", query)
                .form("per_page", searchCount)
                .form("page", 1)
                .execute()) {
            /*
                {
                  "total_results": 10000,
                  "page": 1,
                  "per_page": 1,
                  "photos": [
                    {
                      "id": 3573351,
                      "width": 3066,
                      "height": 3968,
                      "url": "https://www.pexels.com/photo/trees-during-day-3573351/",
                      "photographer": "Lukas Rodriguez",
                      "photographer_url": "https://www.pexels.com/@lukas-rodriguez-1845331",
                      "photographer_id": 1845331,
                      "avg_color": "#374824",
                      "src": {
                        "original": "https://images.pexels.com/photos/3573351/pexels-photo-3573351.png",
                        "large2x": "https://images.pexels.com/photos/3573351/pexels-photo-3573351.png?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940",
                        "large": "https://images.pexels.com/photos/3573351/pexels-photo-3573351.png?auto=compress&cs=tinysrgb&h=650&w=940",
                        "medium": "https://images.pexels.com/photos/3573351/pexels-photo-3573351.png?auto=compress&cs=tinysrgb&h=350",
                        "small": "https://images.pexels.com/photos/3573351/pexels-photo-3573351.png?auto=compress&cs=tinysrgb&h=130",
                        "portrait": "https://images.pexels.com/photos/3573351/pexels-photo-3573351.png?auto=compress&cs=tinysrgb&fit=crop&h=1200&w=800",
                        "landscape": "https://images.pexels.com/photos/3573351/pexels-photo-3573351.png?auto=compress&cs=tinysrgb&fit=crop&h=627&w=1200",
                        "tiny": "https://images.pexels.com/photos/3573351/pexels-photo-3573351.png?auto=compress&cs=tinysrgb&dpr=1&fit=crop&h=200&w=280"
                      },
                      "liked": false,
                      "alt": "Brown Rocks During Golden Hour"
                    }
                  ],
                  "next_page": "https://api.pexels.com/v1/search/?page=2&per_page=1&query=nature"
                }

             */
            if (response.isOk()) {
                JSONObject result = JSONUtil.parseObj(response.body());
                JSONArray photos = result.getJSONArray("photos");
                for (int i = 0; i < photos.size(); i++) {
                    JSONObject photo = photos.getJSONObject(i);
                    JSONObject src = photo.getJSONObject("src");
                    imageList.add(ImageResource.builder()
                            .category(ImageCategoryEnum.CONTENT)
                            .description(photo.getStr("alt", query))
                            .url(src.getStr("medium"))
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("Pexels API 调用失败: {}", e.getMessage(), e);
        }
        return imageList;
    }
}
