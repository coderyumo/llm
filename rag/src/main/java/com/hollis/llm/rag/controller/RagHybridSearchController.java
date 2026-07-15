package com.hollis.llm.rag.controller;

import com.alibaba.cloud.ai.transformer.splitter.RecursiveCharacterTextSplitter;
import com.hollis.llm.rag.cleaner.DocumentCleaner;
import com.hollis.llm.rag.embedding.EmbeddingService;
import com.hollis.llm.rag.es.ElasticSearchService;
import com.hollis.llm.rag.es.EsDocumentChunk;
import com.hollis.llm.rag.reader.DocumentReaderFactory;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;

/**
 * @description:
 * @author: <a href="https://github.com/coderyumo">程序员雨墨</a>
 * @create: 2026-07-15 16:16
 **/
@RestController
@RequestMapping("/rag/hybrid")
public class RagHybridSearchController {


    @Autowired
    private DocumentReaderFactory selector;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private EmbeddingService embeddingService;

    @GetMapping("/write")
    public String write(String filePath) throws Exception {
        // 1. 加载文档
        List<Document> documents = selector.read(new File(filePath));

        // 2. 文本清洗
//        documents = DocumentCleaner.cleanDocuments(documents);

        // 3. 文档分片
        RecursiveCharacterTextSplitter splitter = new RecursiveCharacterTextSplitter(
                // 每块最大字符数
                100,
                // 块之间重叠 100 字符
                new String[]{"。"}
        );
        List<Document> spllitedDocuments = splitter.apply(documents);

        for (Document document : spllitedDocuments) {
            System.out.println(document.getText());
            System.out.println(document.getMetadata());
            System.out.println("=================");
        }

        // 4. 存储到ES
        List<EsDocumentChunk> esDocs = spllitedDocuments.stream().map(doc -> {
            EsDocumentChunk es = new EsDocumentChunk();
            es.setId(doc.getId());
            es.setContent(doc.getText());
            es.setMetadata(doc.getMetadata());
            return es;
        }).toList();

        elasticSearchService.bulkIndex(esDocs);

        // 5. 向量化存储
        embeddingService.embedAndStore(DocumentCleaner.cleanDocuments(spllitedDocuments));
        return "success";
    }

    @RequestMapping("searchFromEs")
    public List<EsDocumentChunk> search(String keyword) throws Exception {
        return elasticSearchService.searchByKeyword(keyword);
    }

    @RequestMapping("searchFromVectorStore")
    public List<Document> searchFromVectorStore(String keyword) throws Exception {
        return embeddingService.similaritySearch(keyword);
    }


}
