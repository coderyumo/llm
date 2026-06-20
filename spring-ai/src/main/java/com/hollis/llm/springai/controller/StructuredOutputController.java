package com.hollis.llm.springai.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.hollis.llm.springai.model.Book;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/prompt/struct")
public class StructuredOutputController implements InitializingBean {

    @Autowired
    @Qualifier("dashScopeChatModel")
    private ChatModel chatModel;

    private ChatClient chatClient;


    @GetMapping("/call")
    public String call(String message) {
        PromptTemplate promptTemplate = PromptTemplate.builder().template("请给我推荐几本JAVA有关的书，输出格式：{format}").build();


        BeanOutputConverter<Book> bookBeanOutputConverter = new BeanOutputConverter<>(Book.class);

        String format = chatClient.prompt(promptTemplate.create(Map.of("format",
                new BeanOutputConverter<>(Book.class).getFormat()))).call().content();
        Book book = bookBeanOutputConverter.convert(format);

        System.out.println(book.toString());

        return book.name() + " " + book.author() + " " + book.desc() + " " + book.price() + " " + book.publisher();
    }


    @RequestMapping("/convert")
    public String convert() {
        Book book = chatClient.prompt("请给我推荐几本心理学有关的书")
                .call().entity(Book.class);
        System.out.println(book.toString());
        return book.name() + " 、 " + book.author() + " 、 " + book.desc() + " 、 " + book.price() + " 、 " + book.publisher();
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultOptions(
                        DashScopeChatOptions.builder().temperature(0.7).build()
                )
                .build();
    }
}
