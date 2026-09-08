package com.soumen.openai.controller;


import com.soumen.openai.tool.HelpDeskTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/api")
public class HelpDeskController {

    private final ChatClient chatClient;
    //private final HelpDeskTool helpDeskTool;

    public HelpDeskController(@Qualifier("helpDesktimeChatClient") ChatClient chatClient
                              //HelpDeskTool helpDeskTool
    ){
        this.chatClient = chatClient;
       // this.helpDeskTool = helpDeskTool;
    }

    @GetMapping("/help-desk")
    public ResponseEntity<String> getLocaltime(@RequestHeader("username") String username,
                                               @RequestParam("message") String message){
        return ResponseEntity.ok(chatClient.prompt()
                .advisors(advisorSpec ->
                        advisorSpec.param(CONVERSATION_ID,username))
                        //.tools(helpDeskTool)
                        .toolContext(Map.of("username",username))
                .user(message)
                .call().content());
    }

}
