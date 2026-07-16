package com.hollis.llm.rag.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: <a href="https://github.com/coderyumo">程序员雨墨</a>
 * @create: 2026-07-16 16:13
 **/
@RestController
@RequestMapping("/rag/modular")
public class ModularRagController implements InitializingBean {

    @Autowired
    private ChatModel chatModel;

    private ChatClient client;

    @Autowired
    private VectorStore vectorStore;


    @GetMapping("/ask")
    public String ask(String question) {

        DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(5)
                .similarityThreshold(0.6)
                .build();
        // 问题改写
        RewriteQueryTransformer queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(ChatClient.builder(chatModel))
                .promptTemplate(new PromptTemplate("""
                        Given a user query, rewrite it to provide better results when querying a {target}.
                                                
                        Remove any irrelevant information, and ensure the query is concise and specific.
                                                
                        如果有表述不清的内容，或者错别字，请修正，如"华子"，修改为"华为"
                                                
                        Original query:
                        {query}
                                                
                        Rewritten query:
                        """))
                .build();

        // 问题拆解
        QueryExpander queryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(ChatClient.builder(chatModel))
                .numberOfQueries(3)
                .includeOriginal(true)
                .build();

        QueryAugmenter queryAugmenter = ContextualQueryAugmenter.builder()
                .allowEmptyContext(true)
                .emptyContextPromptTemplate(new PromptTemplate("请回答以下用户问题"))
                .build();


        RetrievalAugmentationAdvisor questionAnswerAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .queryTransformers(queryTransformer)
                .queryExpander(queryExpander)
                .queryAugmenter(queryAugmenter)
                .build();

        return client.prompt(question).advisors(questionAnswerAdvisor).call().content();
    }


    @Override
    public void afterPropertiesSet() throws Exception {

        client = ChatClient.builder(chatModel)
                .build();
    }
}
