package com.soumen.openai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PromptTemplateController {

    private final ChatClient chatClient;

    public PromptTemplateController(ChatClient chatClient){
        this.chatClient= chatClient;
    }

    @Value("classpath:/prompt-templates/user-prompt-template.st")
    Resource promptTemplate ;

    @GetMapping("/email")
    public String chat(@RequestParam("customerName") String customerName,
                       @RequestParam("customerMsg") String customerMsg){
        return chatClient.prompt()
                .system("""
                        You are a professional customer service assistant which helps drafting email
                        responses to improve the productivity of the customer support team
                        """)
                .user(promptUserSpec -> promptUserSpec.text(promptTemplate)
                        .param("customerName",customerName)
                        .param("customerMsg",customerMsg))
                .call().content();
    }
}
