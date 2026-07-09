package com.hollis.llm.rag.cleaner;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: <a href="https://github.com/coderyumo">程序员雨墨</a>
 * @create: 2026-07-08 17:08
 **/
public class DocumentCleaner {

    public static List<Document> cleanDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return documents;
        }

        return documents.stream()
                .map(doc -> {
                    if (doc == null || doc.getText() == null) {
                        return doc;
                    }

                    String text = doc.getText();

                    // 1. 去掉多余空白字符（空格、制表符、换行等）
                    text = text.replaceAll("\\s+", " ").trim();

                    // 2. 去掉无意义的乱码或特殊符号
                    text = text.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}\\n]", "");

                    // 3. 可选：统一大小写
                    // text = text.toLowerCase();

                    // 4. 按换行拆分段落，去除重复段落
                    String[] paragraphs = text.split("\\n+");
                    Set<String> seen = new LinkedHashSet<>();
                    for (String para : paragraphs) {
                        String trimmed = para.trim();
                        if (!trimmed.isEmpty()) {
                            seen.add(trimmed);
                        }
                    }

                    text = String.join("\n", seen);

                    return new Document(text);
                })
                .collect(Collectors.toList());
    }


    /**
     * 文档分片
     */
    public List<Document> split(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return Collections.emptyList();
        }

        TokenTextSplitter splitter = new TokenTextSplitter(
                // 每块最多 600 tokens
                600,
                // 每块至少 400 字符再考虑断点
                300,
                // 太短的不做嵌入
                5,
                // 最多拆分8000块
                8000,
                // 保留句号、换行符
                true
        );

        return splitter.apply(documents);
    }
}
