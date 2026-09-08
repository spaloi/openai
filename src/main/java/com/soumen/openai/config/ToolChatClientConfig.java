package com.soumen.openai.config;

import com.soumen.openai.custom.advisor.TokenUsageAdvisor;
import com.soumen.openai.tool.TimeTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ToolChatClientConfig {

    @Bean("timeChatClient")
    public ChatClient timeChatClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory
            , TimeTool timeTool){
        Advisor loggingAdvisor = SimpleLoggerAdvisor.builder().build();
        Advisor memoryAdvisor =  MessageChatMemoryAdvisor.builder(chatMemory).build();
        Advisor tokerUsaAdvisor = new TokenUsageAdvisor();
        return chatClientBuilder
                .defaultTools(timeTool)
                .defaultAdvisors(List.of(memoryAdvisor,tokerUsaAdvisor,loggingAdvisor))
                .build();
    }
}
