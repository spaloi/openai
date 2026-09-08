package com.soumen.openai.controller;

import com.soumen.openai.model.CountryCity;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StructuredOutputController {

    private final ChatClient chatClient;

    public StructuredOutputController(ChatClient.Builder chatClientBuilder){
        this.chatClient= chatClientBuilder.defaultAdvisors(new SimpleLoggerAdvisor()).build();
    }

    @GetMapping("/structureData")
    public ResponseEntity<CountryCity> structureData(@RequestParam("message") String message){
        CountryCity countryCity =  chatClient.prompt()
                .user(message)
                .call().entity(CountryCity.class);
        return ResponseEntity.ok(countryCity);
    }

    @GetMapping("/structureDataList")
    public ResponseEntity<List<CountryCity>> structureDataList(@RequestParam("message") String message){
        return ResponseEntity.ok(chatClient.prompt()
                .user(message)
                .call().entity(new ParameterizedTypeReference<List<CountryCity>>(){}));
    }
}
