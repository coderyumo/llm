package com.hollis.llm.langchain4j.controller;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: <a href="https://github.com/coderyumo">程序员雨墨</a>
 * @create: 2026-07-19 14:05
 **/
@RestController
@RequestMapping("/web/search")
public class WebSearchController {

    @Autowired
    OpenAiChatModel chatModel;

    @RequestMapping("/query")
    public String webSearch() {
        // 1、配置搜索引擎
        TavilyWebSearchEngine searchEngine = TavilyWebSearchEngine.builder()
                .apiKey("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")
                .includeAnswer(true)
                .searchDepth("advanced")
                .build();


        // 2、配置web搜索检索器
        WebSearchContentRetriever webSearchContentRetriever = WebSearchContentRetriever.builder()
                .webSearchEngine(searchEngine)
                .maxResults(5)
                .build();

        // 问题改写
        CompressingQueryTransformer queryTransformer = CompressingQueryTransformer.builder()
                .chatModel(chatModel)
                .build();

//        // 问题扩展
//        ExpandingQueryTransformer queryExpander = ExpandingQueryTransformer.builder()
//                .chatModel(chatModel)
//                .build();

        // 3、配置 RetrieverAugmentor
        DefaultRetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .contentRetriever(webSearchContentRetriever)
                .queryTransformer(queryTransformer)
                .build();

        // 4、创建Ai Service

        interface WebSearchAssistant {
            String chat(String question);
        }

        // 5、调用Ai Service

        WebSearchAssistant assistant = AiServices.builder(WebSearchAssistant.class)
                .chatModel(chatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .build();

        return assistant.chat("2026年人工智能有哪些重大突破");
    }

}
