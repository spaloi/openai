package com.soumen.openai.custom.advisor;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;

public class TokenUsageAdvisor implements CallAdvisor {

    public  static final Logger logger = LoggerFactory.getLogger(TokenUsageAdvisor.class);

    @Override
    public @NonNull ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
        ChatResponse chatResponse= chatClientResponse.chatResponse();
        if(chatResponse != null){
           Usage usage= chatResponse.getMetadata().getUsage();
           logger.info("token usage details {}",usage.toString());
        }
        return chatClientResponse;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
