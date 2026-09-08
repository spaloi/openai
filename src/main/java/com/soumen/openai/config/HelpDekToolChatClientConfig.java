package com.soumen.openai.config;

import com.soumen.openai.custom.advisor.TokenUsageAdvisor;
import com.soumen.openai.tool.HelpDeskTool;
import com.soumen.openai.tool.TimeTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.execution.DefaultToolExecutionExceptionProcessor;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;

@Configuration
public class HelpDekToolChatClientConfig {

    @Value("classpath:/prompt-templates/helpDeskSystemPromptTemplate.st")
    Resource systemPromptTemplate;

    @Bean("helpDesktimeChatClient")
    public ChatClient timeChatClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory,
                                     HelpDeskTool helpDeskTool){
        Advisor loggingAdvisor = SimpleLoggerAdvisor.builder().build();
        Advisor memoryAdvisor =  MessageChatMemoryAdvisor.builder(chatMemory).build();
        Advisor tokerUsaAdvisor = new TokenUsageAdvisor();
        return chatClientBuilder
                .defaultSystem(systemPromptTemplate)
                .defaultTools(helpDeskTool)
                .defaultAdvisors(List.of(memoryAdvisor,tokerUsaAdvisor,loggingAdvisor))
                .build();
    }

    @Bean
    ToolExecutionExceptionProcessor toolExecutionExceptionProcessor(){
        return new DefaultToolExecutionExceptionProcessor(true);
    }
}
