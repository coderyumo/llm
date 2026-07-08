package com.hollis.llm.rag.reader;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
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

public class PDFReaderStrategy implements DocumentReaderStrategy {

    @Override
    public boolean supports(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".pdf");
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
        Resource resource = new FileSystemResource(file);

        // 读取配置
        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                .withPageTopMargin(50)         // 忽略顶部50个单位的页眉
                .withPageBottomMargin(50)      // 忽略底部50个单位的页脚
                .withPagesPerDocument(1)       // 每一页作为一个 Document
                .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder()
                        .withNumberOfTopTextLinesToDelete(0) // 每页再额外删掉前0行
                        .build())
                .build();
        PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(resource, config);
        return pagePdfDocumentReader.read();
    }
}
