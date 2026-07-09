package com.hollis.llm.rag.controller;

import com.hollis.llm.rag.cleaner.DocumentCleaner;
import com.hollis.llm.rag.reader.DocumentReaderFactory;
import com.hollis.llm.rag.splitter.OverlapParagraphTextSplitter;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @description:
 * @author: <a href="https://github.com/coderyumo">程序员雨墨</a>
 * @create: 2026-07-08 16:17
 **/
@RestController
@RequestMapping("/rag")
public class RagController {

    @Autowired
    private DocumentReaderFactory documentReaderFactory;


    /**
     * 读取文件
     */
    @GetMapping("/read")
    public List<Document> readDocument(@RequestParam("path") String path) {
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("文件不存在或不是有效文件: " + path);
        }
        try {
            List<Document> documents = documentReaderFactory.read(file);
            return DocumentCleaner.cleanDocuments(documents);
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败: " + e.getMessage(), e);
        }
    }


    /**
     * 读取文件
     */
    @GetMapping("/chunker")
    public String chunker(@RequestParam("path") String path) {
        File file = new File(path);
        List<Document> documents;
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("文件不存在或不是有效文件: " + path);
        }
        try {
            documents = DocumentCleaner.cleanDocuments(documentReaderFactory.read(new File(path)));
            for (Document document : documents) {
                System.out.println("before chunk : " + document.getText());
                System.out.println("=================");
                OverlapParagraphTextSplitter tokenTextSplitter = new OverlapParagraphTextSplitter(
                        100,
                        5
                );
                List<Document> documentList = tokenTextSplitter.split(document);
                for (Document document1 : documentList) {
                    System.out.println("after chunk : " + document1.getText());
                }
                System.out.println("=================");
            }
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败: " + e.getMessage(), e);
        }
        return "success";
    }

}
