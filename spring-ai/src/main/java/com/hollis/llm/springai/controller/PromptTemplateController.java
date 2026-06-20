package com.hollis.llm.springai.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: <a href="https://github.com/coderyumo">程序员雨墨</a>
 * @create: 2026-06-20 15:16
 **/
@RestController
@RequestMapping("/promptTemplate")
public class PromptTemplateController implements InitializingBean {

    @Qualifier("dashScopeChatModel")
    @Autowired
    private ChatModel chatModel;

    private ChatClient chatClient;


    @Value("classpath:/template/open-source-system-prompt.st")
    private Resource template;

    @GetMapping("/file")
    public Flux<String> file(@RequestParam(value = "message") String message, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");

        HashMap variables = new HashMap();
        variables.put("language", "Java");
        variables.put("topic", message);
        PromptTemplate promptTemplate = PromptTemplate.builder().resource(template).variables(variables).build();

        return chatClient.prompt(promptTemplate.create(Map.of("topic", message))).system("你是一个专业的的github项目收集人员").stream().content();
    }

    @GetMapping("call")
    public String call(String topic) {
        String template = "请给我推荐几个关于{topic}的开源项目";
        PromptTemplate promptTemplate = new PromptTemplate(template);
        promptTemplate.add("topic", topic);
        return chatClient.prompt(promptTemplate.create()).call().content();
    }

    @GetMapping("stream")
    public Flux<String> stream(String message, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");

        PromptTemplate promptTemplate = new PromptTemplate("请给我推荐几个关于{topic}的开源项目");
        promptTemplate.add("topic", message);

        return chatClient.prompt(promptTemplate.create(Map.of("topic", message))).system("你是一个专业的的github项目收集人员").stream().content();
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        chatClient = ChatClient.builder(chatModel)
                .defaultOptions(
                        DashScopeChatOptions.builder().temperature(0.7)
                                .build()
                ).build();
    }
}
