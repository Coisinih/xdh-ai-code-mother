package com.xdh.xdhaicodemother.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * @author huanglina
 * date: 2026/8/24
 */
public interface ProjectDownloadService {
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}
