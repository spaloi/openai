package com.soumen.openai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final ChatClient chatClient;
    private final ChatClient webSearchRAGChatClient;
    private final VectorStore vectorStore;

    public RagController(@Qualifier("memoryChatClient")  ChatClient chatClient,
                         @Qualifier("webSearchRAGChatClient")  ChatClient webSearchRAGChatClient,
                         VectorStore vectorStore){
        this.chatClient=chatClient;
        this.webSearchRAGChatClient=webSearchRAGChatClient;
        this.vectorStore=vectorStore;
    }
    @Value("classpath:/prompt-templates/systemPromptRandomDataTemplate.st")
    Resource promptTemplate;

    @Value("classpath:/prompt-templates/hrPolicyPromptTemplate.st")
    Resource hrPromptTemplate;



    @GetMapping("/random-chat")
    public ResponseEntity<String> ragRandomChat(@RequestHeader("userName") String username,
                                             @RequestParam("message") String message){

       String ans = chatClient.prompt()
               .advisors(a->a.param(CONVERSATION_ID,username))
               .user(message).call().content();

     return ResponseEntity.ok(ans);
    }

    @GetMapping("/hrpolicy-chat")
    public ResponseEntity<String> ragHrPolicyChat(@RequestHeader("userName") String username,
                                                @RequestParam("message") String message){
        /*SearchRequest searchRequest= SearchRequest.builder()
                .query(message)
                .topK(3)
                .similarityThreshold(0.5).build();
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        String SimilarContext = documents.stream().map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));*/
        String ans = chatClient.prompt()
                /*.system(promptSystemSpec -> promptSystemSpec
                        .text(hrPromptTemplate)
                        .param("documents",SimilarContext))*/
                .advisors(a->a.param(CONVERSATION_ID,username))
                .user(message).call().content();

        return ResponseEntity.ok(ans);
    }

    @GetMapping("/webserch-chat")
    public ResponseEntity<String> webSerchChat(@RequestHeader("userName") String username,
                                                  @RequestParam("message") String message){
        String ans = webSearchRAGChatClient.prompt()
                .advisors(a->a.param(CONVERSATION_ID,username))
                .user(message).call().content();
        return ResponseEntity.ok(ans);
    }

}
