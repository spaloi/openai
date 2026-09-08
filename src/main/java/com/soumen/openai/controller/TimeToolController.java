package com.soumen.openai.controller;

import com.soumen.openai.custom.advisor.TokenUsageAdvisor;
import com.soumen.openai.tool.TimeTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/api")
public class TimeToolController {
    private final ChatClient chatClient;

    public TimeToolController(@Qualifier("timeChatClient") ChatClient chatClient){
        this.chatClient = chatClient;
    }

    @GetMapping("/local-time")
    public ResponseEntity<String> getLocaltime(@RequestHeader("userName") String username,
                                             @RequestParam("message") String message){
        return ResponseEntity.ok(chatClient.prompt()
                .advisors(advisorSpec ->
                        advisorSpec.param(CONVERSATION_ID,username))
                .user(message)
                .call().content());
    }



}
