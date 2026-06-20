package com.hollis.llm.springai.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @description:
 * @author: <a href="https://github.com/coderyumo">程序员雨墨</a>
 * @create: 2026-06-19 15:22
 **/
@RestController
@RequestMapping("/chatClient")
public class ChatClientController implements InitializingBean {

    @Qualifier("dashScopeChatModel")
    @Autowired
    private ChatModel chatModel;

    private ChatClient chatClient;

    @RequestMapping("/simpleCall")
    public String simpleCall(String message) {
        return chatClient.prompt(message).call().content();
    }

    @RequestMapping("/callOverWrite")
    public String callOverWrite(String message) {
        return chatClient.prompt(message).system("加上3").call().content();
    }


    @RequestMapping("/callUser")
    public String callUser(String message) {
        return chatClient.prompt().user(message).call().content();
    }

    @GetMapping("/stream")
    public Flux<String> stream(String message) {
        return chatClient.prompt(message).stream().content();
    }


    @RequestMapping("/call")
    public String call(String message) {
        // 会追加
        SystemMessage systemMessage = new SystemMessage("加上3");
        UserMessage userMessage = new UserMessage(message);
        return chatClient.prompt(new Prompt(systemMessage, userMessage)).call().content();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultSystem("请用英文回答问题")
                .defaultOptions(
                        DashScopeChatOptions.builder().temperature(0.7).build()
                )
                .build();
    }
}

