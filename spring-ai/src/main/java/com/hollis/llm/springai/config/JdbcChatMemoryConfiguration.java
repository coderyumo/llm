package com.hollis.llm.springai.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author: <a href="https://github.com/coderyumo">程序员雨墨</a>
 * @create: 2026-06-23 15:40
 **/
@Configuration
public class JdbcChatMemoryConfiguration {

    
    @Bean
    public ChatMemory jdbcChatMemory(JdbcChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder().chatMemoryRepository(chatMemoryRepository).maxMessages(20).build();
    }
}
