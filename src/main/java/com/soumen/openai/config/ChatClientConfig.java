package com.soumen.openai.config;

import com.soumen.openai.custom.advisor.TokenUsageAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder){
       //var option= OpenAiChatOptions.builder().model("gpt-5.4-mini")
              // .temperature(0.8).maxCompletionTokens(1000);*/

        return chatClientBuilder.defaultAdvisors( new TokenUsageAdvisor())
        .defaultAdvisors(new SimpleLoggerAdvisor())
                //.defaultOptions(option)
                /*.defaultSystem("""
                        You are an internal IT helpdesk assistant. Your role is to assist
                        employees with IT-related issues such as resetting passwords,
                        unlocking accounts, and answering questions related to IT policies.
                        If a user requests help with anything outside of these
                        responsibilities, respond politely and inform them that you are
                        only able to assist with IT support tasks within your defined scope.
                        """)*/
                .build();
    }
}
