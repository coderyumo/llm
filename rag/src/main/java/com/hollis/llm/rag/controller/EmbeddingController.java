package com.hollis.llm.rag.controller;

import com.hollis.llm.rag.cleaner.DocumentCleaner;
import com.hollis.llm.rag.embedding.EmbeddingService;
import com.hollis.llm.rag.reader.DocumentReaderFactory;
import com.hollis.llm.rag.splitter.OverlapParagraphTextSplitter;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: <a href="https://github.com/coderyumo">程序员雨墨</a>
 * @create: 2026-07-11 14:39
 **/
@RestController
@RequestMapping("/embedding")
public class EmbeddingController {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private DocumentReaderFactory documentReaderFactory;

    @Autowired
    private EmbeddingService embeddingService;

    @RequestMapping("/test")
    public String test() {
        for (float i : embeddingModel.embed("test")) {
            System.out.println(i);
        }
        return "success";
    }

    @RequestMapping("/embed")
    public String embed(String filePath) {

        List<Document> documents;
        try {
            documents = documentReaderFactory.read(new File(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //清洗并分段
        List<Document> allChunkedDocuments = DocumentCleaner.cleanDocuments(documents).stream()
                .flatMap(document -> {
                    OverlapParagraphTextSplitter splitter = new OverlapParagraphTextSplitter(1000, 50);
                    return splitter.split(document).stream();
                })
                .collect(Collectors.toList());

        //向量化并存储
        embeddingService.embedAndStore(DocumentCleaner.cleanDocuments(allChunkedDocuments));

        return "success";
    }
}
