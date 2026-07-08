package com.hollis.llm.rag.reader;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @description:
 * @author: <a href="https://github.com/coderyumo">程序员雨墨</a>
 * @create: 2026-07-08 15:41
 **/
@Component
public interface DocumentReaderStrategy {

    /**
     * 判断是否支持该文件
     */
    boolean supports(File file);

    /**
     * 读取文件并返回 Document 列表
     */
    List<Document> read(File file) throws IOException;
}
