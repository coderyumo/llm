package com.hollis.llm.rag.controller;

import com.alibaba.cloud.ai.transformer.splitter.RecursiveCharacterTextSplitter;
import com.hollis.llm.rag.cleaner.DocumentCleaner;
import com.hollis.llm.rag.embedding.EmbeddingService;
import com.hollis.llm.rag.es.ElasticSearchService;
import com.hollis.llm.rag.es.EsDocumentChunk;
import com.hollis.llm.rag.reader.DocumentReaderFactory;
import com.hollis.llm.rag.rerank.ReRankUtil;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;
import java.util.Map;

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

    @RequestMapping("searchFromHybrid")
    public List<String> searchFromHybrid(String keyword) throws Exception {
        List<Document> vectorDocuments = embeddingService.similaritySearch(keyword);
        System.out.println(vectorDocuments);
        System.out.println("=========================");

        List<EsDocumentChunk> esDocumentChunks = elasticSearchService.searchByKeyword(keyword);
        System.out.println(esDocumentChunks);
        System.out.println("=========================");

        List<String> result = ReRankUtil.rerankFusion(vectorDocuments, esDocumentChunks, keyword, 5);
        System.out.println(result);
        System.out.println("=========================");

        return result;

    }

    @Autowired
    private ChatModel chatModel;

    @RequestMapping("chatToHybrid")
    public String chatToHybrid(String keyword) throws Exception {

        String newQuestion = chatModel.call(
                """
                        你是一个问题改写大师，请改写用户的问题，使其更具体、更详细。
                        如果其中有错别字，请你直接做修改。
                        用户问题：
                                                
                        """ + keyword
        );

        System.out.println(newQuestion);

        List<EsDocumentChunk> esDocs = elasticSearchService.searchByKeyword(newQuestion);

        List<Document> vectorDocs = embeddingService.similaritySearch(newQuestion);

        List<String> result = ReRankUtil.rerankFusion(vectorDocs, esDocs, newQuestion, 5);

        String prompt = """
                请根据以下文档内容，回答用户的问题。
                注意，你只能参考文档内容回答，不要自己做推理。
                文档内容：
                {contents}
                                
                用户问题:
                {question}
                """;

        return chatModel.call(new PromptTemplate(prompt).create(Map.of("contents", result, "question", newQuestion))).getResult().getOutput().getText();
    }

}
