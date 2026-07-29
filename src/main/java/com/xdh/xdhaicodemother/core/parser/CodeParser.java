package com.xdh.xdhaicodemother.core.parser;

/**
 * 代码解析器策略接口，将原始的输入解析撑特定类型的代码对象 - 策略模式
 *
 * @author huanglina
 * date: 2026/7/29
 */
public interface CodeParser<T> {
    /**
     * 解析代码内容
     *
     * @param codeContent 原始的代码内容
     * @return 解析后的代码对象
     */
    T codeParse(String codeContent);
}
