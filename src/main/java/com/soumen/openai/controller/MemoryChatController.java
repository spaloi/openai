package com.soumen.openai.controller;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/api")
public class MemoryChatController {

    private final ChatClient chatClient;

    public MemoryChatController(@Qualifier("memoryChatClient") ChatClient chatClient){
        this.chatClient= chatClient;
    }
    @GetMapping("/chat-memory")
    public ResponseEntity<String> chatMemory(@RequestHeader("userName") String username,
            @RequestParam("message") String message){
          return ResponseEntity.ok(chatClient.prompt()
                          .advisors(advisorSpec ->
                                       advisorSpec.param(ChatMemory.CONVERSATION_ID,username))
                  .user(message)
                  .call().content());
    }

    
}
