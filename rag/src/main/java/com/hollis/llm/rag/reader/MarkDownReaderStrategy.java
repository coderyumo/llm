package com.hollis.llm.rag.reader;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @description:
 * @author: <a href="https://github.com/coderyumo">程序员雨墨</a>
 * @create: 2026-07-08 15:43
 **/
@Service
public class MarkDownReaderStrategy implements DocumentReaderStrategy {

    @Override
    public boolean supports(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".md");
    }

    /**
     * 文本读取器
     *
     * @param file
     * @return
     * @throws IOException
     */
    @Override
    public List<Document> read(File file) throws IOException {
        // 读取配置
        Resource resource = new FileSystemResource(file);
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                // 水平线分割生成新文档
                .withHorizontalRuleCreateDocument(true)
                // 不包含代码块
                .withIncludeCodeBlock(false)
                // 不包含引用
                .withIncludeBlockquote(false)
                // 添加文件名元数据
                .withAdditionalMetadata("filename", file.getName())
                .build();
        return new MarkdownDocumentReader(resource, config).get();
    }
}
