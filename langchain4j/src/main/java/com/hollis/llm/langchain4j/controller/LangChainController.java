package com.hollis.llm.langchain4j.controller;

import com.hollis.llm.langchain4j.service.LangChainAiService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

/**
 * @description:
 * @author: <a href="https://github.com/coderyumo">程序员雨墨</a>
 * @create: 2026-07-19 14:50
 **/
@RestController
@RequestMapping("/langchain/rag")
public class LangChainController {

    @Autowired
    private OpenAiChatModel chatModel;

    @RequestMapping("/query")
    public String retriever(String query, String filePath) {
        // 加载 文档
        Document document = loadDocument(filePath, new ApacheTikaDocumentParser());

        DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(300, 50);

        // 切分
        List<TextSegment> segments = splitter.split(document);

        // 创建 embedding 模型,把文档向量化
        OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .modelName("text-embedding-v4")  // 阿里云 DashScope 的 embedding 模型名称
                .dimensions(768)  // text-embedding-v4 支持 768 维度
                .maxSegmentsPerBatch(9)
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey("sk-ws-H.REHIDMR.CNbB.MEUCICLPGNjORqo690TPxr5hdFwUhFdIQek0Wz3lakGdPKZvAiEAnaolf593IuZY27Wo0PcTkFjJO8FjmXU7El9hdGtovLk")
                .build();
        List<Embedding> content = embeddingModel.embedAll(segments).content();


        // 创建向量数据库，基于内存
        EmbeddingStore embeddingStore = new InMemoryEmbeddingStore();
        embeddingStore.addAll(content, segments);

        // 创建内容检索器
        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .build();

        //5.构建上下文融合器
        DefaultContentInjector contentInjector = new DefaultContentInjector(new PromptTemplate("""
                 ## 角色定位
                 你是一位专业的RAG问答助手。请根据提供的上下文信息，详细、准确地回答用户的问题。如果参考文档没有内容，请务必不要胡编乱造，请直接说明"没有找到相关信息"。
                                
                 ## 任务要求：
                 1. 请基于以下提供的参考文档内容，回答用户的问题。
                 2. 如果参考文档中没有相关信息，请直接说明"没有找到相关信息"，不要编造内容。
                 3. 如果有了参考文档内容，请务必尽量回答问题。有可能用户的输入比较随意，你可以先尝试回答用户的问题，猜测他的实际需求，先给出回复，你需要尽量去贴合用户的问题需求。
                                
                 ## 格式要求：
                 1. 你的所有回答必须使用Markdown格式进行排版。
                 2. 上下文信息中包含了图片描述标签，格式为：`<image src="URL" description="多模态描述"></image>`。
                 3. 如果图片与用户提问高度相关，请将此标签转换为标准的Markdown图片格式 `![图片](URL)`。
                 4. 仅在必要时包含图片，请注意千万不要输出重复的内容和图片，图片确保最终生成的URL不要重复。
                                
                 ## 参考文档:
                {{contents}}
                                
                 ## 用户问题:
                 {{userMessage}}
                                
                 注意：如果参考文档下面的内容为空，请直接回答“没有找到相关信息”。
                """));

        // 创建检索增强器
        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .contentInjector(contentInjector)
                .contentRetriever(contentRetriever)
                .build();

        LangChainAiService aiService = AiServices.builder(LangChainAiService.class)
                .retrievalAugmentor(retrievalAugmentor)
                .chatModel(chatModel)
                .build();
        return aiService.chat(query);
    }
}
