package com.soumen.openai.controller;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/api")
public class OpenChatCacheController {

    private ChatClient chatClient;

    OpenChatCacheController(@Qualifier("cacheChatClint") ChatClient chatClient){
        this.chatClient= chatClient;
    }

    @GetMapping("/open-chat")
    public String chatMemory(@RequestParam("message") String message){
        return chatClient.prompt()
                .user(message)
                .call().content();
    }


}
