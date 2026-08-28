package com.xdh.xdhaicodemother.utils;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;

/**
 * 缓存 key 生成工具类
 *
 * @author xdh
 * @since 2026/8/28
 */
public class CacheKeyUtils {

    /**
     * 根据对象生成缓存key (JSON + MD5)
     *
     * @param obj 要生成key的对象
     * @return MD5哈希后的缓存key
     */
    public static String generateKey(Object obj) {
        if (obj == null) {
            return DigestUtil.md5Hex("null");
        }
        // 先转JSON，再MD5
        // 1.JSON序列化:确保对象内容的一致性，相同内容的对象生成相同的字符串
        // 2.MD5哈希:将长字符串转换为固定长度的字符串，避免 Rediskey 过长占用过多资源
        String jsonStr = JSONUtil.toJsonStr(obj);
        return DigestUtil.md5Hex(jsonStr);
    }
}
